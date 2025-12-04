package org.lcr.nvp.domain.member.dto

import java.time.LocalDate

data class SignupRequest(
    val email: String,
    val password: String,
    val name: String
)

data class LoginRequest(
    val email: String,
    val password: String
)
