package org.lcr.nvp.domain.member.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "회원 직책 할당 요청 DTO")
data class AssignPositionRequest(
    @Schema(description = "할당할 부서의 ID. (전체 목록: 회장단, 훈련부, 매니저, 총무부, 관리부, 일반)", example = "1")
    val departmentId: Long,
    @Schema(description = "할당할 직책의 ID. (전체 목록: 파트장, 차장, 일반, 게스트)", example = "2")
    val positionId: Long,
    @Schema(description = "할당할 기간(기수)의 ID. (예: 13=37기)", example = "13")
    val periodId: Long
)

@Schema(description = "회원 상태 변경 요청 DTO")
data class UpdateMemberStatusRequest(
    @Schema(description = "변경할 회원 상태", example = "ACTIVE_MEMBER, ALUMNI")
    val membershipStatus: String
)

@Schema(description = "사용자 역할 변경 요청 DTO")
data class UpdateUserRolesRequest(
    @Schema(description = "새롭게 부여할 역할 목록", example = "[\"ROLE_MEMBER\", \"ROLE_MANAGER\"]")
    val roles: List<String>
)
