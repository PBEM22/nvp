package org.lcr.nvp.domain.member.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "이메일 중복 확인 응답 DTO")
data class EmailCheckResponse(
    @Schema(description = "해당 이메일의 사용 가능 여부", example = "true")
    val isAvailable: Boolean
)
