package org.lcr.nvp.domain.match.repository

import org.lcr.nvp.domain.match.domain.Tournament
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TournamentRepository : JpaRepository<Tournament, Long> {
    fun findAllByOrderByIdDesc(): List<Tournament>
}
