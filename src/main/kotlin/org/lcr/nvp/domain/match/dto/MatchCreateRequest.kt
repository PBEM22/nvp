package org.lcr.nvp.domain.match.dto

import java.time.LocalDate

data class MatchCreateRequest(
    val tournamentId: Long,
    val opponentSchoolId: Long,
    val isMale: Boolean,
    val matchLocation: String,
    val matchDate: LocalDate
)
