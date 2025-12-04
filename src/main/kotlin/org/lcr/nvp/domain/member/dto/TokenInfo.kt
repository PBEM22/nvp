package org.lcr.nvp.domain.member.dto

import io.swagger.v3.oas.annotations.media.Schema

/**
 * 서비스 계층에서 컨트롤러로 AccessToken과 RefreshToken을 함께 전달하기 위한 DTO
 */
@Schema(description = "JWT 토큰 정보 DTO")
data class TokenInfo(
    @Schema(description = "발급된 Access Token")
    val accessToken: String,
    @Schema(description = "발급된 Refresh Token")
    val refreshToken: String
)

/**
 * 토큰 재발급 성공 시, 새로운 AccessToken을 담아 클라이언트에게 반환하는 DTO
 */
@Schema(description = "토큰 재발급 응답 DTO")
data class AccessTokenResponse(
    @Schema(description = "새로 발급된 Access Token")
    val accessToken: String
)
