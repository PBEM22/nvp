package org.lcr.nvp.domain.attendance.application

import org.lcr.nvp.domain.attendance.domain.Attendance
import org.lcr.nvp.domain.attendance.domain.AttendanceStatus
import org.lcr.nvp.domain.attendance.domain.ExerciseDate
import org.lcr.nvp.domain.attendance.dto.CheckInRequest
import org.lcr.nvp.domain.attendance.dto.GenerateCodeResponse
import org.lcr.nvp.domain.attendance.repository.AttendanceRepository
import org.lcr.nvp.domain.attendance.repository.ExerciseDateRepository
import org.lcr.nvp.domain.member.repository.MemberAssignmentRepository
import org.lcr.nvp.domain.member.repository.MemberRepository
import org.lcr.nvp.domain.member.repository.PeriodRepository
import org.lcr.nvp.domain.member.repository.UserRepository
import org.lcr.nvp.global.exception.BusinessException
import org.lcr.nvp.global.exception.ErrorCode
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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
     */
    fun generateAttendanceCode(round: Int): GenerateCodeResponse {
        if (round !in 1..2) {
            throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)
        }
        if (redisTemplate.hasKey(ATTENDANCE_CODE_KEY)) {
            throw BusinessException(ErrorCode.ATTENDANCE_CODE_ALREADY_EXISTS)
        }

        // 오늘 날짜의 ExerciseDate를 찾거나 생성합니다.
        val today = LocalDate.now()
        val exerciseDate = exerciseDateRepository.findByDate(today)
            ?: exerciseDateRepository.save(ExerciseDate(date = today))

        // 6자리 숫자 코드 생성
        val code = String.format("%06d", Random.nextInt(1_000_000))

        // Redis에 코드 정보 저장
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
            throw BusinessException(ErrorCode.INVALID_ATTENDANCE_CODE)
        }

        val round = codeData["round"]!!.toInt()
        val exerciseDateId = codeData["exerciseDateId"]!!.toLong()

        val user = userRepository.findByEmail(userEmail)
            ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)
        val member = memberRepository.findByUser(user)
            ?: throw BusinessException(ErrorCode.MEMBER_NOT_FOUND)
        val exerciseDate = exerciseDateRepository.findById(exerciseDateId)
            .orElseThrow { BusinessException(ErrorCode.EXERCISE_DATE_NOT_FOUND) }

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
            .orElseThrow { BusinessException(ErrorCode.MEMBER_NOT_FOUND) }

        val exerciseDate = exerciseDateRepository.findByDate(request.date)
            ?: throw BusinessException(ErrorCode.EXERCISE_DATE_NOT_FOUND)

        val newStatus = try {
            AttendanceStatus.valueOf(request.status)
        } catch (e: IllegalArgumentException) {
            throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)
        }

        val attendance = attendanceRepository.findByMemberAndExerciseDate(member, exerciseDate)
            ?: attendanceRepository.save(Attendance(member = member, exerciseDate = exerciseDate))

        when (request.round) {
            1 -> attendance.round1Status = newStatus
            2 -> attendance.round2Status = newStatus
            else -> throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)
        }
    }

    /**
     * 특정 날짜의 현재 활동 기수 회원들의 출석 현황을 조회합니다.
     */
    @Transactional(readOnly = true)
    fun getDailyAttendanceStatus(date: LocalDate): List<org.lcr.nvp.domain.attendance.dto.DailyAttendanceStatusResponse> {
        // 1. 해당 날짜의 운동일 정보 조회
        val exerciseDate = exerciseDateRepository.findByDate(date) ?: return emptyList()

        // 2. 현재 활동 기수(Period) 정보 조회
        val currentPeriod = periodRepository.findByIsCurrent(true)
            ?: throw BusinessException(ErrorCode.PERIOD_NOT_FOUND) // 현재 활동 기수가 설정되지 않았으면 에러

        // 3. 현재 활동 기수에 속한 모든 회원 조회
        val currentMembers = memberAssignmentRepository.findAllByPeriodWithMember(currentPeriod)
            .map { it.member }
            .distinctBy { it.id } // 중복 회원 제거 (한 기수에 여러 직책을 가질 수 있으므로)

        // 4. 해당 날짜의 모든 출석 기록을 Map으로 변환 (조회 성능 향상)
        val attendanceMap = attendanceRepository.findAllByExerciseDateWithMember(exerciseDate)
            .associateBy { it.member.id }

        // 5. 현재 활동 기수 회원들을 기준으로 최종 응답 DTO 리스트 생성
        return currentMembers.map { member ->
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
}

