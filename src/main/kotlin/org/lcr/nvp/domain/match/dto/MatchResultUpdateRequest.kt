package org.lcr.nvp.domain.match.dto

data class MatchResultUpdateRequest(
    val isWin: Boolean,
    val teamScore: Int,
    val opponentScore: Int
)
