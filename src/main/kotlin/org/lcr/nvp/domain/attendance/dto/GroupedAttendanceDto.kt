package org.lcr.nvp.domain.attendance.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "기수 정보 DTO")
data class PeriodInfo(
    @Schema(description = "연도", example = "2025")
    val year: Int,
    @Schema(description = "학기", example = "1")
    val semester: Int,
    @Schema(description = "기수 번호", example = "37")
    val number: Int
)

@Schema(description = "기수별 출석률 요약 정보 DTO")
data class PeriodAttendanceSummary(
    @Schema(description = "해당 기수의 총 운동일수", example = "10")
    val totalExerciseDays: Int,
    @Schema(description = "해당 기수의 총 출석일수", example = "8")
    val totalPresentDays: Int,
    @Schema(description = "해당 기수의 총 지각일수", example = "1")
    val totalLateDays: Int,
    @Schema(description = "해당 기수의 총 조퇴일수", example = "0")
    val totalEarlyLeaveDays: Int,
    @Schema(description = "해당 기수의 총 결석일수", example = "1")
    val totalAbsentDays: Int,
    @Schema(description = "해당 기수의 전체 출석률 (소수점 2자리)", example = "80.00")
    val attendanceRate: Double
)

@Schema(description = "기수별 출석 상세 내역 DTO")
data class PeriodAttendance(
    @Schema(description = "기수 정보")
    val period: PeriodInfo,
    @Schema(description = "해당 기수의 출석 요약 정보")
    val summary: PeriodAttendanceSummary,
    @Schema(description = "해당 기수의 날짜별 출석 상세 내역")
    val details: List<MyAttendanceDetailResponse>
)

@Schema(description = "회원 본인의 기수별 그룹화된 출석률 및 상세 내역 응답 DTO")
data class GroupedMyAttendanceResponse(
    @Schema(description = "현재 동아리 활동 기수. 활성화된 기수가 없으면 null", example = "37")
    val currentPeriodNumber: Int?,
    @Schema(description = "기수별로 그룹화된 출석 내역 목록")
    val attendanceByPeriods: List<PeriodAttendance>
)
