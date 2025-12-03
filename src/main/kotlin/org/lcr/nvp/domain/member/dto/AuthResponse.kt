package org.lcr.nvp.domain.member.dto

data class LoginResponse(
    val accessToken: String,
    val memberId: Long?
)
