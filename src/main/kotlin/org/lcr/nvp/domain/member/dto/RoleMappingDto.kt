package org.lcr.nvp.domain.member.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "역할 매핑 정보 응답 DTO")
data class RoleMappingResponse(
    @Schema(description = "매핑 ID", example = "1")
    val id: Long,
    @Schema(description = "부서 ID", example = "1")
    val departmentId: Long,
    @Schema(description = "부서명", example = "회장단")
    val departmentName: String,
    @Schema(description = "직책 ID", example = "1")
    val positionId: Long,
    @Schema(description = "직책명", example = "파트장")
    val positionName: String,
    @Schema(description = "표시 이름", example = "회장")
    val displayName: String
)

@Schema(description = "역할 매핑 생성 요청 DTO")
data class CreateRoleMappingRequest(
    @Schema(description = "부서 ID", example = "1")
    val departmentId: Long,
    @Schema(description = "직책 ID", example = "1")
    val positionId: Long,
    @Schema(description = "표시 이름", example = "회장")
    val displayName: String
)

@Schema(description = "역할 매핑 수정 요청 DTO")
data class UpdateRoleMappingRequest(
    @Schema(description = "새로운 표시 이름", example = "대표")
    val displayName: String
)
