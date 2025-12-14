package org.lcr.nvp.domain.member.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "사용자 계정 상태 변경 요청 DTO")
data class UpdateUserAccountStatusRequest(
    @Schema(description = "변경할 상태 (예: ACTIVE, SUSPENDED)", example = "SUSPENDED")
    @field:NotBlank
    val status: String
)
