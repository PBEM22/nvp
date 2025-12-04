package org.lcr.nvp.domain.member.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "로그인 응답 DTO")
data class LoginResponse(
    @Schema(description = "발급된 Access Token")
    val accessToken: String,
    @Schema(description = "정식 회원인 경우의 memberId. 정식 회원이 아니면 null.", example = "1", nullable = true)
    val memberId: Long?
)
