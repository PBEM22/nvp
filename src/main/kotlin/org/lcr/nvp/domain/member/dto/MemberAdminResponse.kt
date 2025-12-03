package org.lcr.nvp.domain.member.dto

import java.time.LocalDate

data class MemberSummaryResponse(
    val memberId: Long,
    val userId: Long,
    val email: String,
    val name: String,
    val membershipStatus: String
)

data class MemberDetailResponse(
    val memberId: Long,
    val userId: Long,
    val email: String,
    val name: String,
    val birthday: LocalDate,
    val isMale: Boolean,
    val backNumber: Int?,
    val isPublic: Boolean,
    val membershipStatus: String,
    val assignments: List<AssignmentHistoryDto>
)

data class AssignmentHistoryDto(
    val departmentName: String,
    val positionName: String,
    val periodYear: Int,
    val periodSemester: Int
)
