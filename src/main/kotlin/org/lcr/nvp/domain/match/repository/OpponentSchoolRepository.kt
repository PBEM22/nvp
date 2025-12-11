package org.lcr.nvp.domain.match.repository

import org.lcr.nvp.domain.match.domain.OpponentSchool
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OpponentSchoolRepository : JpaRepository<OpponentSchool, Long> {
    fun findAllByOrderBySchoolNameAsc(): List<OpponentSchool>
}
