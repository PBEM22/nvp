package org.lcr.nvp.domain.member.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "내 정보 수정 요청 DTO")
data class UpdateMyInfoRequest(
    @Schema(description = "새로운 이름", example = "김엔비", required = false)
    val name: String?,

    @Schema(description = "새로운 생년월일", example = "2000-01-15", required = false)
    val birthday: LocalDate?,

    @Schema(description = "새로운 성별 (남성/여성)", example = "남성", required = false)
    val gender: String?,

    @Schema(description = "새로운 등번호", example = "13", required = false)
    val backNumber: Int?,

    @Schema(description = "새로운 학과", example = "컴퓨터공학과", required = false)
    val major: String?
)
