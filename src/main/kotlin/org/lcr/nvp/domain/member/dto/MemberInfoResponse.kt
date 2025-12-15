package org.lcr.nvp.domain.member.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "회원 목록 조회(상세)를 위한 응답 DTO")
data class MemberInfoResponse(
    @Schema(description = "회원 ID", example = "1")
    val memberId: Long,
    @Schema(description = "이름", example = "김엔비")
    val name: String?,
    @Schema(description = "이메일", example = "test@example.com")
    val email: String,
    @Schema(description = "등번호", example = "10", nullable = true)
    val backNumber: Int?,
    @Schema(description = "학과", example = "컴퓨터공학과", nullable = true)
    val major: String?,
    @Schema(description = "회원 상태", example = "ACTIVE_MEMBER")
    val membershipStatus: String,

    // 최신 활동 이력
    @Schema(description = "최신 기수", example = "37", nullable = true)
    val periodNumber: Int?,
    @Schema(description = "최신 활동 연도", example = "2024", nullable = true)
    val periodYear: Int?,
    @Schema(description = "최신 부서명", example = "훈련부", nullable = true)
    val departmentName: String?,
    @Schema(description = "최신 직책명", example = "파트장", nullable = true)
    val positionName: String?,
    @Schema(description = "최신 표시 이름", example = "훈련부장", nullable = true)
    val displayName: String?
)
