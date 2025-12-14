package org.lcr.nvp.domain.match.repository

import org.lcr.nvp.domain.match.domain.Match
import org.lcr.nvp.domain.match.domain.OpponentSchool
import org.lcr.nvp.domain.match.domain.Tournament
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface MatchRepository : JpaRepository<Match, Long> {

    @Query("SELECT m FROM Match m JOIN FETCH m.tournament JOIN FETCH m.opponentSchool WHERE m.tournament.id = :tournamentId ORDER BY m.matchDate DESC")
    fun findMatchesByTournamentIdWithDetails(@Param("tournamentId") tournamentId: Long): List<Match>

    @Query("SELECT DISTINCT m FROM Match m JOIN m.records r JOIN FETCH m.tournament JOIN FETCH m.opponentSchool WHERE r.member.id = :memberId ORDER BY m.matchDate DESC")
    fun findDistinctMatchesByMemberIdWithDetails(@Param("memberId") memberId: Long): List<Match>
}
