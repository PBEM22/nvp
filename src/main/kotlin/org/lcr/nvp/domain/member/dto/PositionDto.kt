package org.lcr.nvp.domain.member.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "직책 정보 응답 DTO")
data class PositionResponse(
    @Schema(description = "직책 ID", example = "1")
    val id: Long,
    @Schema(description = "직책명", example = "파트장")
    val name: String
)

@Schema(description = "직책 생성 요청 DTO")
data class CreatePositionRequest(
    @Schema(description = "새로 생성할 직책명", example = "수습")
    val name: String
)

@Schema(description = "직책 수정 요청 DTO")
data class UpdatePositionRequest(
    @Schema(description = "수정할 직책명", example = "정회원")
    val name: String
)
