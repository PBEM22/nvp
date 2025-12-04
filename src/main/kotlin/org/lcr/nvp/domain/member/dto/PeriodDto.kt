package org.lcr.nvp.domain.member.dto

import io.swagger.v3.oas.annotations.media.Schema

// ===== Request DTOs =====

@Schema(description = "기수(Period) 생성 요청 DTO")
data class CreatePeriodRequest(
    @Schema(description = "연도", example = "2025")
    val year: Int,
    @Schema(description = "학기 (1 또는 2)", example = "1")
    val semester: Int,
    @Schema(description = "기수 번호 (고유값)", example = "16")
    val periodNumber: Int
)

@Schema(description = "기수(Period) 수정 요청 DTO")
data class UpdatePeriodRequest(
    @Schema(description = "연도", example = "2025")
    val year: Int,
    @Schema(description = "학기 (1 또는 2)", example = "1")
    val semester: Int,
    @Schema(description = "기수 번호 (고유값)", example = "16")
    val periodNumber: Int
)


// ===== Response DTOs =====

@Schema(description = "기수(Period) 정보 응답 DTO")
data class PeriodResponse(
    @Schema(description = "기수 ID", example = "1")
    val id: Long,
    @Schema(description = "연도", example = "2025")
    val year: Int,
    @Schema(description = "학기", example = "1")
    val semester: Int,
    @Schema(description = "기수 번호", example = "16")
    val periodNumber: Int,
    @Schema(description = "현재 활동 기수 여부", example = "true")
    val isCurrent: Boolean
)
