package org.lcr.nvp.domain.member.application

import org.lcr.nvp.domain.member.domain.Period
import org.lcr.nvp.domain.member.dto.CreatePeriodRequest
import org.lcr.nvp.domain.member.dto.PeriodResponse
import org.lcr.nvp.domain.member.dto.UpdatePeriodRequest
import org.lcr.nvp.domain.member.repository.PeriodRepository
import org.lcr.nvp.global.exception.BusinessException
import org.lcr.nvp.global.exception.ErrorCode
import org.lcr.nvp.global.exception.domain.PeriodNotFoundException
import org.lcr.nvp.global.exception.domain.PeriodNumberDuplicationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class PeriodService(
    private val periodRepository: PeriodRepository
) {

    fun createPeriod(request: CreatePeriodRequest): Period {
        if (periodRepository.findByPeriodNumber(request.periodNumber) != null) {
            throw PeriodNumberDuplicationException()
        }
        val period = Period(
            year = request.year,
            semester = request.semester,
            periodNumber = request.periodNumber
        )
        return periodRepository.save(period)
    }

    @Transactional(readOnly = true)
    fun getAllPeriods(): List<PeriodResponse> {
        return periodRepository.findAllByOrderByPeriodNumberDesc().map { period ->
            PeriodResponse(
                id = period.id,
                year = period.year,
                semester = period.semester,
                periodNumber = period.periodNumber,
                isCurrent = period.isCurrent
            )
        }
    }

    @Transactional(readOnly = true)
    fun getCurrentPeriod(): PeriodResponse {
        val period = periodRepository.findByIsCurrent(true)
            ?: throw BusinessException(ErrorCode.CURRENT_PERIOD_NOT_SET)
        return PeriodResponse(
            id = period.id,
            year = period.year,
            semester = period.semester,
            periodNumber = period.periodNumber,
            isCurrent = period.isCurrent
        )
    }

    fun updatePeriod(periodId: Long, request: UpdatePeriodRequest): Period {
        val period = periodRepository.findById(periodId)
            .orElseThrow { PeriodNotFoundException() }

        // 수정하려는 기수 번호가 현재 기수 번호와 다른데, 다른 기수가 이미 사용 중인 번호일 경우 예외 처리
        if (period.periodNumber != request.periodNumber && periodRepository.findByPeriodNumber(request.periodNumber) != null) {
            throw PeriodNumberDuplicationException()
        }

        period.year = request.year
        period.semester = request.semester
        period.periodNumber = request.periodNumber

        return periodRepository.save(period)
    }

    fun deletePeriod(periodId: Long) {
        val period = periodRepository.findById(periodId)
            .orElseThrow { PeriodNotFoundException() }

        // TODO: 해당 기수에 할당된 회원이 있는지 확인하는 로직 추가 필요
        // if (memberAssignmentRepository.existsByPeriod(period)) {
        //     throw BusinessException(...)
        // }

        periodRepository.delete(period)
    }

    /**
     * 특정 기수를 현재 활동 기수로 설정합니다.
     * 기존의 현재 활동 기수는 해제됩니다.
     */
    fun setCurrentPeriod(periodId: Long) {
        // 1. 기존에 '현재 기수'로 설정된 것이 있다면 해제
        periodRepository.findByIsCurrent(true)?.let { currentPeriod ->
            if (currentPeriod.id != periodId) {
                currentPeriod.isCurrent = false
            }
        }

        // 2. 새로 지정된 기수를 '현재 기수'로 설정
        val newCurrentPeriod = periodRepository.findById(periodId)
            .orElseThrow { PeriodNotFoundException() }
        newCurrentPeriod.isCurrent = true
    }
}
