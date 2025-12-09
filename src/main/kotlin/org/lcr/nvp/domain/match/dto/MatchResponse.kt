package org.lcr.nvp.domain.match.dto

import org.lcr.nvp.domain.match.domain.Match
import java.time.LocalDate

data class MatchResponse(
    val id: Long,
    val tournamentName: String,
    val opponentDisplayName: String, // opponentSchoolName -> opponentDisplayName
    val isMale: Boolean,
    val isWin: Boolean,
    val teamScore: Int,
    val opponentScore: Int,
    val matchMvp: String?,
    val matchLocation: String?,
    val matchDate: LocalDate?
) {
    companion object {
        fun from(match: Match): MatchResponse {
            val opponentDisplayName = "${match.opponentSchool.schoolName} (${match.opponentSchool.teamName})"
            return MatchResponse(
                id = match.id!!,
                tournamentName = match.tournament.tournamentName,
                opponentDisplayName = opponentDisplayName,
                isMale = match.isMale,
                isWin = match.isWin,
                teamScore = match.teamScore,
                opponentScore = match.opponentScore,
                matchMvp = match.matchMvp,
                matchLocation = match.matchLocation,
                matchDate = match.matchDate
            )
        }
    }
}
