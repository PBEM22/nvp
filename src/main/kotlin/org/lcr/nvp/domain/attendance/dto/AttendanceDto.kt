package org.lcr.nvp.domain.attendance.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

// ===== Request DTOs =====

@Schema(description = "출석 코드 생성 요청 DTO")
data class GenerateCodeRequest(
    @Schema(description = "출석 회차 (1 또는 2)", example = "1")
    val round: Int
)

@Schema(description = "출석 체크인 요청 DTO")
data class CheckInRequest(
    @Schema(description = "사용자가 입력한 6자리 출석 코드", example = "123456")
    val code: String
)

@Schema(description = "운영진용 출석 상태 수동 변경 요청 DTO")
data class UpdateAttendanceRequest(
    @Schema(description = "대상 회원의 memberId", example = "1")
    val memberId: Long,
    @Schema(description = "대상 날짜", example = "2025-12-05")
    val date: LocalDate,
    @Schema(description = "변경할 회차 (1 또는 2)", example = "1")
    val round: Int,
    @Schema(description = "변경할 상태 (PRESENT 또는 ABSENT)", example = "PRESENT")
    val status: String
)

// ===== Response DTOs =====

@Schema(description = "생성된 출석 코드 응답 DTO")
data class GenerateCodeResponse(
    @Schema(description = "생성된 6자리 출석 코드", example = "123456")
    val code: String,
    @Schema(description = "코드 만료 시간(초)", example = "600")
    val expiresIn: Long
)

@Schema(description = "특정 날짜의 회원별 출석 현황 DTO")
data class DailyAttendanceStatusResponse(
    @Schema(description = "회원 ID", example = "1")
    val memberId: Long,
    @Schema(description = "회원 이름", example = "홍길동")
    val memberName: String,
    @Schema(description = "1회차 출석 상태", example = "PRESENT")
    val round1Status: String,
    @Schema(description = "2회차 출석 상태", example = "ABSENT")
    val round2Status: String,
    @Schema(description = "최종 출석 상태", example = "조퇴")
    val finalStatus: String
)

@Schema(description = "회원 본인의 출석률 요약 정보 DTO")
data class MyAttendanceSummaryResponse(
    @Schema(description = "총 운동일수", example = "10")
    val totalExerciseDays: Int,
    @Schema(description = "총 출석일수", example = "8")
    val totalPresentDays: Int,
    @Schema(description = "총 지각일수", example = "1")
    val totalLateDays: Int,
    @Schema(description = "총 조퇴일수", example = "0")
    val totalEarlyLeaveDays: Int,
    @Schema(description = "총 결석일수", example = "1")
    val totalAbsentDays: Int,
    @Schema(description = "전체 출석률 (소수점 2자리)", example = "80.00")
    val attendanceRate: Double
)

@Schema(description = "회원 본인의 날짜별 출석 상세 정보 DTO")
data class MyAttendanceDetailResponse(
    @Schema(description = "운동 날짜", example = "2025-12-01")
    val date: LocalDate,
    @Schema(description = "1회차 출석 상태", example = "PRESENT")
    val round1Status: String,
    @Schema(description = "2회차 출석 상태", example = "PRESENT")
    val round2Status: String,
    @Schema(description = "최종 출석 상태", example = "출석")
    val finalStatus: String,
    @Schema(description = "해당 출석 기록의 연도", example = "2025", nullable = true)
    val periodYear: Int?,
    @Schema(description = "해당 출석 기록의 학기", example = "1", nullable = true)
    val periodSemester: Int?,
    @Schema(description = "해당 출석 기록의 기수 번호", example = "16", nullable = true)
    val periodNumber: Int?
)

@Schema(description = "회원 본인의 출석률 및 상세 내역 응답 DTO")
data class MyAttendanceResponse(
    @Schema(description = "출석률 요약 정보")
    val summary: MyAttendanceSummaryResponse,
    @Schema(description = "날짜별 출석 상세 내역")
    val details: List<MyAttendanceDetailResponse>
)
