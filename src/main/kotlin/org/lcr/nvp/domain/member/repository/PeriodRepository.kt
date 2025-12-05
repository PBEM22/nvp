package org.lcr.nvp.domain.member.repository

import org.lcr.nvp.domain.member.domain.Period
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PeriodRepository : JpaRepository<Period, Long> {
    fun findByIsCurrent(isCurrent: Boolean): Period?
    fun findByPeriodNumber(periodNumber: Int): Period?
    fun findAllByOrderByPeriodNumberDesc(): List<Period>
}
