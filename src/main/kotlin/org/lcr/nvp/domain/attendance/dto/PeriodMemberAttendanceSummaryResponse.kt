package org.lcr.nvp.domain.attendance.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "특정 기수에 속한 한 회원의 출석 요약 정보")
data class PeriodMemberAttendanceSummaryResponse(
    @Schema(description = "회원 ID")
    val memberId: Long,
    @Schema(description = "회원 이름", nullable = true)
    val memberName: String?,
    @Schema(description = "총 운동일 수")
    val totalExerciseDays: Int,
    @Schema(description = "출석일 수")
    val presentDays: Int,
    @Schema(description = "지각일 수")
    val lateDays: Int,
    @Schema(description = "조퇴일 수")
    val earlyLeaveDays: Int,
    @Schema(description = "결석일 수")
    val absentDays: Int,
    @Schema(description = "출석률 (%)")
    val attendanceRate: Double
)
