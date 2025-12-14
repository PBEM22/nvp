package org.lcr.nvp.domain.attendance.application

import org.lcr.nvp.domain.attendance.domain.Attendance
import org.lcr.nvp.domain.attendance.domain.AttendanceStatus
import org.lcr.nvp.domain.attendance.domain.ExerciseDate
import org.lcr.nvp.domain.attendance.dto.*
import org.lcr.nvp.domain.attendance.repository.AttendanceRepository
import org.lcr.nvp.domain.attendance.repository.ExerciseDateRepository
import org.lcr.nvp.domain.member.repository.MemberAssignmentRepository
import org.lcr.nvp.domain.member.repository.MemberRepository
import org.lcr.nvp.domain.member.repository.PeriodRepository
import org.lcr.nvp.domain.member.repository.UserRepository
import org.lcr.nvp.global.exception.BusinessException
import org.lcr.nvp.global.exception.ErrorCode
import org.lcr.nvp.global.exception.domain.*
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import kotlin.random.Random

@Service
@Transactional
class AttendanceService(
    private val exerciseDateRepository: ExerciseDateRepository,
    private val attendanceRepository: AttendanceRepository,
    private val userRepository: UserRepository,
    private val memberRepository: MemberRepository,
    private val periodRepository: PeriodRepository,
    private val memberAssignmentRepository: MemberAssignmentRepository,
    private val redisTemplate: RedisTemplate<String, String>
) {
    companion object {
        private const val ATTENDANCE_CODE_KEY = "attendance:code"
        private const val CODE_EXPIRATION_MINUTES: Long = 10
    }

    /**
     * 운영진이 출석 코드를 생성합니다.
     * 기존에 유효한 코드가 있다면 덮어씁니다.
     */
    fun generateAttendanceCode(round: Int): GenerateCodeResponse {
        if (round !in 1..2) {
            throw InvalidInputValueException()
        }

        // 오늘 날짜의 ExerciseDate를 찾거나 생성합니다.
        val today = LocalDate.now()
        val exerciseDate = exerciseDateRepository.findByDate(today)
            ?: exerciseDateRepository.save(ExerciseDate(date = today))

        // 6자리 숫자 코드 생성
        val code = String.format("%06d", Random.nextInt(1_000_000))

        // Redis에 코드 정보 저장 (기존 코드가 있으면 덮어씀)
        val hashOps = redisTemplate.opsForHash<String, String>()
        val codeData = mapOf(
            "code" to code,
            "round" to round.toString(),
            "exerciseDateId" to exerciseDate.id.toString()
        )
        hashOps.putAll(ATTENDANCE_CODE_KEY, codeData)
        redisTemplate.expire(ATTENDANCE_CODE_KEY, CODE_EXPIRATION_MINUTES, TimeUnit.MINUTES)

        return GenerateCodeResponse(code, CODE_EXPIRATION_MINUTES * 60)
    }

    /**
     * 회원이 출석 코드를 제출하여 체크인합니다.
     */
    fun checkIn(userEmail: String, request: CheckInRequest) {
        val hashOps = redisTemplate.opsForHash<String, String>()
        val codeData = hashOps.entries(ATTENDANCE_CODE_KEY)

        if (codeData.isEmpty() || codeData["code"] != request.code) {
            throw InvalidAttendanceCodeException()
        }

        val round = codeData["round"]!!.toInt()
        val exerciseDateId = codeData["exerciseDateId"]!!.toLong()

        val user = userRepository.findByProviderId(userEmail)
            ?: userRepository.findByEmail(userEmail)
            ?: throw UserNotFoundException()
        val member = memberRepository.findByUser(user)
            ?: throw MemberNotFoundException()
        val exerciseDate = exerciseDateRepository.findById(exerciseDateId)
            .orElseThrow { ExerciseDateNotFoundException() }

        // 해당 날짜에 대한 회원의 출석 기록을 찾거나 새로 생성합니다.
        val attendance = attendanceRepository.findByMemberAndExerciseDate(member, exerciseDate)
            ?: attendanceRepository.save(Attendance(member = member, exerciseDate = exerciseDate))

        // 회차에 따라 상태를 업데이트합니다.
        when (round) {
            1 -> attendance.round1Status = AttendanceStatus.PRESENT
            2 -> attendance.round2Status = AttendanceStatus.PRESENT
        }

        // 한번 사용된 코드는 즉시 삭제하여 중복 사용을 방지합니다.
        redisTemplate.delete(ATTENDANCE_CODE_KEY)
    }

    /**
     * 운영진이 현재 활성화된 출석 코드를 즉시 만료시킵니다.
     */
    fun invalidateAttendanceCode() {
        val wasDeleted = redisTemplate.delete(ATTENDANCE_CODE_KEY)
        if (!wasDeleted) {
            // 이미 코드가 만료되었거나 없는 경우, 에러를 발생시키지 않고 성공으로 처리하거나
            // 혹은 클라이언트에게 "만료시킬 코드가 없습니다" 와 같은 메시지를 전달할 수 있습니다.
            // 현재는 별도 처리 없이 성공으로 간주합니다.
        }
    }

    /**
     * 운영진이 회원의 출석 상태를 수동으로 변경합니다.
     */
    fun updateAttendanceStatus(request: org.lcr.nvp.domain.attendance.dto.UpdateAttendanceRequest) {
        val member = memberRepository.findById(request.memberId)
            .orElseThrow { MemberNotFoundException() }

        val exerciseDate = exerciseDateRepository.findByDate(request.date)
            ?: throw ExerciseDateNotFoundException()

        val newStatus = try {
            AttendanceStatus.valueOf(request.status)
        } catch (e: IllegalArgumentException) {
            throw InvalidInputValueException()
        }

        val attendance = attendanceRepository.findByMemberAndExerciseDate(member, exerciseDate)
            ?: attendanceRepository.save(Attendance(member = member, exerciseDate = exerciseDate))

        when (request.round) {
            1 -> attendance.round1Status = newStatus
            2 -> attendance.round2Status = newStatus
            else -> throw InvalidInputValueException()
        }
    }

    /**
     * 특정 날짜의 특정 기수 회원들의 출석 현황을 조회합니다.
     * periodId가 null이면 현재 활동 기수를 기준으로 조회합니다.
     */
    @Transactional(readOnly = true)
    fun getDailyAttendanceStatus(date: LocalDate, periodId: Long?): List<org.lcr.nvp.domain.attendance.dto.DailyAttendanceStatusResponse> {
        // 1. 해당 날짜의 운동일 정보 조회
        val exerciseDate = exerciseDateRepository.findByDate(date) ?: return emptyList()

        // 2. 조회할 기수(Period) 정보 결정
        val targetPeriod = if (periodId != null) {
            periodRepository.findById(periodId).orElseThrow { PeriodNotFoundException() }
        } else {
            periodRepository.findByIsCurrent(true) ?: throw BusinessException(ErrorCode.CURRENT_PERIOD_NOT_SET)
        }

        // 3. 해당 기수에 속한 모든 회원 조회
        val membersInPeriod = memberAssignmentRepository.findAllByPeriodWithMember(targetPeriod)
            .map { it.member }
            .distinctBy { it.id } // 중복 회원 제거 (한 기수에 여러 직책을 가질 수 있으므로)

        // 4. 해당 날짜의 모든 출석 기록을 Map으로 변환 (조회 성능 향상)
        val attendanceMap = attendanceRepository.findAllByExerciseDateWithMember(exerciseDate)
            .associateBy { it.member.id }

        // 5. 해당 기수 회원들을 기준으로 최종 응답 DTO 리스트 생성
        return membersInPeriod.map { member ->
            val attendance = attendanceMap[member.id]
            if (attendance != null) {
                // 출석 기록이 있는 경우
                org.lcr.nvp.domain.attendance.dto.DailyAttendanceStatusResponse(
                    memberId = member.id,
                    memberName = member.user.name,
                    round1Status = attendance.round1Status.name,
                    round2Status = attendance.round2Status.name,
                    finalStatus = attendance.getFinalStatus()
                )
            } else {
                // 출석 기록이 없는 경우 (결석 처리)
                org.lcr.nvp.domain.attendance.dto.DailyAttendanceStatusResponse(
                    memberId = member.id,
                    memberName = member.user.name,
                    round1Status = AttendanceStatus.ABSENT.name,
                    round2Status = AttendanceStatus.ABSENT.name,
                    finalStatus = "결석"
                )
            }
        }
    }

    @Transactional(readOnly = true)
    fun getAttendanceSummaryByPeriod(periodId: Long): List<PeriodMemberAttendanceSummaryResponse> {
        // 1. 기수 정보 조회
        val period = periodRepository.findById(periodId)
            .orElseThrow { PeriodNotFoundException() }

        // 2. 해당 기수에 속한 모든 회원 조회
        val membersInPeriod = memberAssignmentRepository.findAllByPeriodWithMember(period)
            .map { it.member }
            .distinctBy { it.id }

        if (membersInPeriod.isEmpty()) {
            return emptyList()
        }

        // 3. 해당 기수의 기간(연도, 학기)에 해당하는 모든 운동일 조회
        val startMonth = if (period.semester == 1) 1 else 7
        val endMonth = if (period.semester == 1) 6 else 12
        val exerciseDatesInPeriod = exerciseDateRepository.findByYearAndSemester(period.year, startMonth, endMonth)

        if (exerciseDatesInPeriod.isEmpty()) {
            // 운동일이 없으면 모든 회원의 출석률은 0%
            return membersInPeriod.map { member ->
                PeriodMemberAttendanceSummaryResponse(
                    memberId = member.id,
                    memberName = member.user.name,
                    totalExerciseDays = 0, presentDays = 0, lateDays = 0, earlyLeaveDays = 0, absentDays = 0, attendanceRate = 0.0
                )
            }
        }

        // 4. 해당 회원들과 운동일에 대한 모든 출석 기록을 한 번에 조회
        val attendances = attendanceRepository.findAllByMemberInAndExerciseDateIn(membersInPeriod, exerciseDatesInPeriod)
        val attendancesByMemberId = attendances.groupBy { it.member.id }

        // 5. 회원별로 출석 요약 계산
        return membersInPeriod.map { member ->
            val memberAttendances = attendancesByMemberId[member.id] ?: emptyList()

            var present = 0
            var late = 0
            var earlyLeave = 0

            memberAttendances.forEach { attendance ->
                when {
                    attendance.round1Status == AttendanceStatus.PRESENT && attendance.round2Status == AttendanceStatus.PRESENT -> present++
                    attendance.round1Status == AttendanceStatus.ABSENT && attendance.round2Status == AttendanceStatus.PRESENT -> late++
                    attendance.round1Status == AttendanceStatus.PRESENT && attendance.round2Status == AttendanceStatus.ABSENT -> earlyLeave++
                }
            }

            val totalExerciseDays = exerciseDatesInPeriod.size
            val absent = totalExerciseDays - memberAttendances.size + (memberAttendances.size - (present + late + earlyLeave))

            val attendanceRate = if (totalExerciseDays > 0) {
                (BigDecimal(present + late + earlyLeave) / BigDecimal(totalExerciseDays) * BigDecimal(100))
                    .setScale(2, RoundingMode.HALF_UP)
                    .toDouble()
            } else {
                0.0
            }

            PeriodMemberAttendanceSummaryResponse(
                memberId = member.id,
                memberName = member.user.name,
                totalExerciseDays = totalExerciseDays,
                presentDays = present,
                lateDays = late,
                earlyLeaveDays = earlyLeave,
                absentDays = absent,
                attendanceRate = attendanceRate
            )
        }
    }

    /**
     * 회원 본인의 출석률 및 상세 내역을 기수별로 그룹화하여 조회합니다.
     */
    @Transactional(readOnly = true)
    fun getMyAttendance(userEmail: String): org.lcr.nvp.domain.attendance.dto.GroupedMyAttendanceResponse {
        val user = userRepository.findByProviderId(userEmail)
            ?: userRepository.findByEmail(userEmail)
            ?: throw UserNotFoundException()
        val member = memberRepository.findByUser(user)
            ?: throw MemberNotFoundException()

        // 1. 현재 활동 기수 정보 및 모든 기수 정보 조회
        val currentPeriod = periodRepository.findByIsCurrent(true)
        val allPeriods = periodRepository.findAll()
        val periodMap = allPeriods.associateBy { period ->
            // 1학기: 1월~6월, 2학기: 7월~12월로 가정
            "${period.year}-${period.semester}"
        }

        // 2. 회원의 모든 출석 기록 조회
        val myAttendances = attendanceRepository.findByMemberWithExerciseDate(member)

        // 3. 출석 기록을 Period를 키로 하는 맵으로 수동 그룹화
        val attendancesByPeriod = mutableMapOf<org.lcr.nvp.domain.member.domain.Period, MutableList<Attendance>>()
        myAttendances.forEach { attendance ->
            val exerciseDate = attendance.exerciseDate
            val exerciseYear = exerciseDate.date.year
            val exerciseMonth = exerciseDate.date.monthValue
            val exerciseSemester = if (exerciseMonth in 1..6) 1 else 2
            val periodKey = "${exerciseYear}-${exerciseSemester}"
            val period = periodMap[periodKey]

            if (period != null) {
                attendancesByPeriod.getOrPut(period) { mutableListOf() }.add(attendance)
            }
        }

        // 4. 각 기수별로 출석 정보 처리
        val periodAttendanceList = attendancesByPeriod.map { (period, attendances) ->
            var totalPresentDays = 0
            var totalLateDays = 0
            var totalEarlyLeaveDays = 0
            var totalAbsentDays = 0

            val totalExerciseDaysInPeriod = attendances.size

            val details = attendances.map { attendance ->
                val finalStatus = when {
                    attendance.round1Status == AttendanceStatus.PRESENT && attendance.round2Status == AttendanceStatus.PRESENT -> {
                        totalPresentDays++
                        "출석"
                    }
                    attendance.round1Status == AttendanceStatus.ABSENT && attendance.round2Status == AttendanceStatus.PRESENT -> {
                        totalLateDays++
                        "지각"
                    }
                    attendance.round1Status == AttendanceStatus.PRESENT && attendance.round2Status == AttendanceStatus.ABSENT -> {
                        totalEarlyLeaveDays++
                        "조퇴"
                    }
                    else -> {
                        totalAbsentDays++
                        "결석"
                    }
                }
                org.lcr.nvp.domain.attendance.dto.MyAttendanceDetailResponse(
                    date = attendance.exerciseDate.date,
                    round1Status = attendance.round1Status.name,
                    round2Status = attendance.round2Status.name,
                    finalStatus = finalStatus,
                    periodYear = period.year,
                    periodSemester = period.semester,
                    periodNumber = period.periodNumber
                )
            }.sortedByDescending { it.date }

            val attendanceRate = if (totalExerciseDaysInPeriod > 0) {
                (BigDecimal(totalPresentDays + totalLateDays + totalEarlyLeaveDays) / BigDecimal(totalExerciseDaysInPeriod) * BigDecimal(100))
                    .setScale(2, RoundingMode.HALF_UP)
                    .toDouble()
            } else {
                0.00
            }

            val summary = org.lcr.nvp.domain.attendance.dto.PeriodAttendanceSummary(
                totalExerciseDays = totalExerciseDaysInPeriod,
                totalPresentDays = totalPresentDays,
                totalLateDays = totalLateDays,
                totalEarlyLeaveDays = totalEarlyLeaveDays,
                totalAbsentDays = totalAbsentDays,
                attendanceRate = attendanceRate
            )

            val periodInfo = org.lcr.nvp.domain.attendance.dto.PeriodInfo(
                year = period.year,
                semester = period.semester,
                number = period.periodNumber
            )

            org.lcr.nvp.domain.attendance.dto.PeriodAttendance(
                period = periodInfo,
                summary = summary,
                details = details
            )
        }.sortedByDescending { it.period.number }

        return org.lcr.nvp.domain.attendance.dto.GroupedMyAttendanceResponse(
            currentPeriodNumber = currentPeriod?.periodNumber,
            attendanceByPeriods = periodAttendanceList
        )
    }

    /**
     * 특정 회원의 전체 출석 기록을 조회합니다.
     */
    @Transactional(readOnly = true)
    fun getMemberAttendanceHistory(memberId: Long): List<MemberAttendanceHistoryResponse> {
        val member = memberRepository.findById(memberId)
            .orElseThrow { MemberNotFoundException() }

        val attendances = attendanceRepository.findByMemberWithExerciseDate(member)

        return attendances.map { attendance ->
            MemberAttendanceHistoryResponse(
                date = attendance.exerciseDate.date,
                round1Status = attendance.round1Status.name,
                round2Status = attendance.round2Status.name,
                finalStatus = attendance.getFinalStatus()
            )
        }
    }

    @Transactional(readOnly = true)
    fun getTodayAttendance(userEmail: String): TodayAttendanceResponse {
        val user = userRepository.findByProviderId(userEmail)
            ?: userRepository.findByEmail(userEmail)
            ?: throw UserNotFoundException()
        val member = memberRepository.findByUser(user)
            ?: throw MemberNotFoundException()

        val today = LocalDate.now()
        val exerciseDate = exerciseDateRepository.findByDate(today)

        val attendance = exerciseDate?.let {
            attendanceRepository.findByMemberAndExerciseDate(member, it)
        }

        return if (attendance != null) {
            TodayAttendanceResponse(
                date = today,
                round1Status = attendance.round1Status.name,
                round2Status = attendance.round2Status.name
            )
        } else {
            TodayAttendanceResponse(
                date = today,
                round1Status = AttendanceStatus.ABSENT.name,
                round2Status = AttendanceStatus.ABSENT.name
            )
        }
    }
}

