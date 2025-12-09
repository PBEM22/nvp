package org.lcr.nvp.domain.match.dto

data class OpponentSchoolCreateRequest(
    val schoolName: String,
    val teamName: String,
    val schoolLogoUrl: String?
)
