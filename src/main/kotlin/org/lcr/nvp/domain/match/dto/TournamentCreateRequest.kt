package org.lcr.nvp.domain.match.dto

data class TournamentCreateRequest(
    val tournamentName: String,
    val isSixPlayer: Boolean
)
