package org.lcr.nvp.domain.member.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "회원 목록 조회 시 사용되는 필터 및 검색 조건 DTO")
data class MemberSearchFilter(
    @Schema(description = "활동 연도", example = "2024", required = false)
    val year: Int?,

    @Schema(description = "기수", example = "37", required = false)
    val periodNumber: Int?,

    @Schema(description = "부서 ID", example = "1", required = false)
    val departmentId: Long?,

    @Schema(description = "직책 ID", example = "1", required = false)
    val positionId: Long?,

    @Schema(description = "검색어 (이름 또는 학과)", example = "김엔비", required = false)
    val keyword: String?
)
