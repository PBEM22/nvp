package org.lcr.nvp.domain.member.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "부서 정보 응답 DTO")
data class DepartmentResponse(
    @Schema(description = "부서 ID", example = "1")
    val id: Long,
    @Schema(description = "부서명", example = "훈련부")
    val name: String
)

@Schema(description = "부서 생성 요청 DTO")
data class CreateDepartmentRequest(
    @Schema(description = "새로 생성할 부서명", example = "기획부")
    val name: String
)

@Schema(description = "부서 수정 요청 DTO")
data class UpdateDepartmentRequest(
    @Schema(description = "수정할 부서명", example = "훈련기획부")
    val name: String
)
