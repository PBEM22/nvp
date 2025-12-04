package org.lcr.nvp.domain.member.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "회원 목록 조회를 위한 요약 정보 DTO")
data class MemberSummaryResponse(
    @Schema(description = "회원 고유 ID", example = "1")
    val memberId: Long,
    @Schema(description = "사용자 고유 ID", example = "1")
    val userId: Long,
    @Schema(description = "이메일", example = "test@example.com")
    val email: String,
    @Schema(description = "이름", example = "홍길동")
    val name: String,
    @Schema(description = "회원 상태 (e.g., ACTIVE_MEMBER, ALUMNI)", example = "ACTIVE_MEMBER")
    val membershipStatus: String
)

@Schema(description = "회원 상세 정보 응답 DTO")
data class MemberDetailResponse(
    @Schema(description = "회원 고유 ID", example = "1")
    val memberId: Long,
    @Schema(description = "사용자 고유 ID", example = "1")
    val userId: Long,
    @Schema(description = "이메일", example = "test@example.com")
    val email: String,
    @Schema(description = "이름", example = "홍길동")
    val name: String,
    @Schema(description = "생년월일", example = "2000-01-01")
    val birthday: LocalDate,
    @Schema(description = "성별 (true: 남성, false: 여성)", example = "true")
    val isMale: Boolean,
    @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
    val profileImageUrl: String?,
    @Schema(description = "등번호", example = "10")
    val backNumber: Int?,
    @Schema(description = "연혁 공개 여부", example = "true")
    val isPublic: Boolean,
    @Schema(description = "회원 상태 (e.g., ACTIVE_MEMBER, ALUMNI)", example = "ACTIVE_MEMBER")
    val membershipStatus: String,
    @Schema(description = "역대 활동 이력 목록")
    val assignments: List<AssignmentHistoryDto>
)

@Schema(description = "회원의 역대 활동 이력 DTO")
data class AssignmentHistoryDto(
    @Schema(description = "부서명", example = "훈련부")
    val departmentName: String,
    @Schema(description = "직책명", example = "파트장(훈련부장)")
    val positionName: String,
    @Schema(description = "연도", example = "2024")
    val periodYear: Int,
    @Schema(description = "학기", example = "1")
    val periodSemester: Int,
    @Schema(description = "기수", example = "15")
    val periodNumber: Int
)
