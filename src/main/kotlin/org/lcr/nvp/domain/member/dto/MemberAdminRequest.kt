package org.lcr.nvp.domain.member.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "회원 직책 할당 요청 DTO")
data class AssignPositionRequest(
    @Schema(description = "할당할 부서의 ID (e.g., 회장단, 훈련, 매니저, 총무)", example = "1")
    val departmentId: Long,
    @Schema(description = "할당할 직책의 ID (e.g., 파트장, 차장, 일반)", example = "1")
    val positionId: Long,
    @Schema(description = "할당할 기간(기수)의 ID", example = "1")
    val periodId: Long
)

@Schema(description = "회원 상태 변경 요청 DTO")
data class UpdateMemberStatusRequest(
    @Schema(description = "변경할 회원 상태", example = "ALUMNI")
    val membershipStatus: String
)

@Schema(description = "정식 회원으로 승격 요청 DTO")
data class PromoteMemberRequest(
    @Schema(description = "생년월일", example = "2000-01-01")
    val birthday: LocalDate,
    @Schema(description = "성별 (true: 남성, false: 여성)", example = "true")
    val isMale: Boolean
)
