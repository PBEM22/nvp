package org.lcr.nvp.domain.attendance.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "오늘의 내 출석 상태 응답 DTO")
data class TodayAttendanceResponse(
    @Schema(description = "오늘 날짜")
    val date: LocalDate,
    @Schema(description = "1차 출석 상태", example = "PRESENT")
    val round1Status: String,
    @Schema(description = "2차 출석 상태", example = "ABSENT")
    val round2Status: String
)
