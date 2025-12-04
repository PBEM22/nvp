package org.lcr.nvp.domain.member.dto

import java.time.LocalDate

data class AssignPositionRequest(
    val departmentId: Long,
    val positionId: Long,
    val periodId: Long
)

data class UpdateMemberStatusRequest(
    val membershipStatus: String
)

data class PromoteMemberRequest(
    val birthday: LocalDate,
    val isMale: Boolean
)
