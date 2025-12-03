package org.lcr.nvp.domain.member.dto

/**
 * 서비스 계층에서 컨트롤러로 AccessToken과 RefreshToken을 함께 전달하기 위한 DTO
 */
data class TokenInfo(
    val accessToken: String,
    val refreshToken: String
)

/**
 * 토큰 재발급 성공 시, 새로운 AccessToken을 담아 클라이언트에게 반환하는 DTO
 */
data class AccessTokenResponse(
    val accessToken: String
)
