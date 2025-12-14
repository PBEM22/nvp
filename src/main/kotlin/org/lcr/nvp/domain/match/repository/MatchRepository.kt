package org.lcr.nvp.domain.match.repository

import org.lcr.nvp.domain.match.domain.Match
import org.lcr.nvp.domain.match.domain.OpponentSchool
import org.lcr.nvp.domain.match.domain.Tournament
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface MatchRepository : JpaRepository<Match, Long> {

    @Query("SELECT m FROM Match m JOIN FETCH m.tournament JOIN FETCH m.opponentSchool WHERE m.tournament.id = :tournamentId AND m.deletedAt IS NULL ORDER BY m.matchDate DESC")
    fun findMatchesByTournamentIdWithDetails(@Param("tournamentId") tournamentId: Long): List<Match>

    @Query("SELECT DISTINCT r.match FROM MatchRecord r JOIN FETCH r.match.tournament JOIN FETCH r.match.opponentSchool WHERE r.member.id = :memberId AND r.match.deletedAt IS NULL ORDER BY r.match.matchDate DESC")
    fun findDistinctMatchesByMemberIdWithDetails(@Param("memberId") memberId: Long): List<Match>

    fun findByIdAndDeletedAtIsNull(id: Long): Optional<Match>

    fun existsByTournament(tournament: Tournament): Boolean
}
