package org.lcr.nvp.domain.member.dto

data class AssignPositionRequest(
    val departmentId: Long,
    val positionId: Long,
    val periodId: Long
)

data class UpdateMemberStatusRequest(
    val membershipStatus: String
)
