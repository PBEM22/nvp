package org.lcr.nvp.domain.member.repository

import org.lcr.nvp.domain.member.domain.Position
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PositionRepository : JpaRepository<Position, Long> {
    fun findByName(name: String): Position?
}
