package org.lcr.nvp.model

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val accessToken: String
)
