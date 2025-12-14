package org.lcr.nvp.domain.match.repository

import org.lcr.nvp.domain.match.domain.Tournament
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface TournamentRepository : JpaRepository<Tournament, Long> {
    @Query("SELECT t FROM Tournament t WHERE t.deletedAt IS NULL ORDER BY t.id DESC")
    fun findAllActiveByOrderByIdDesc(): List<Tournament>

    fun findByIdAndDeletedAtIsNull(id: Long): Optional<Tournament>
}
