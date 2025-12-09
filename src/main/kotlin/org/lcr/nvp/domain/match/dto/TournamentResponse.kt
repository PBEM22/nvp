package org.lcr.nvp.domain.match.dto

import org.lcr.nvp.domain.match.domain.Tournament

data class TournamentResponse(
    val id: Long,
    val tournamentName: String,
    val isSixPlayer: Boolean
) {
    companion object {
        fun from(tournament: Tournament): TournamentResponse {
            return TournamentResponse(
                id = tournament.id!!,
                tournamentName = tournament.tournamentName,
                isSixPlayer = tournament.isSixPlayer
            )
        }
    }
}
