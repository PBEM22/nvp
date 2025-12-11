package org.lcr.nvp.domain.attendance.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "특정 회원의 출석 기록 응답 DTO")
data class MemberAttendanceHistoryResponse(
    @Schema(description = "운동 날짜")
    val date: LocalDate,
    @Schema(description = "1라운드 출석 상태")
    val round1Status: String,
    @Schema(description = "2라운드 출석 상태")
    val round2Status: String,
    @Schema(description = "최종 출석 상태 (출석, 지각, 조퇴, 결석)")
    val finalStatus: String
)
