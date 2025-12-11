package org.lcr.nvp.domain.match.dto

import io.swagger.v3.oas.annotations.media.Schema
import org.lcr.nvp.domain.match.domain.OpponentSchool

@Schema(description = "상대 학교 정보 응답 DTO")
data class OpponentSchoolResponse(
    @Schema(description = "상대 학교 ID", example = "1")
    val id: Long,
    @Schema(description = "학교 이름", example = "서울대학교")
    val schoolName: String,
    @Schema(description = "팀 이름", example = "배구부")
    val teamName: String,
    @Schema(description = "학교 로고 이미지 URL", example = "https://example.com/logo.png", nullable = true)
    val schoolLogoUrl: String?
) {
    companion object {
        fun from(opponentSchool: OpponentSchool): OpponentSchoolResponse {
            return OpponentSchoolResponse(
                id = opponentSchool.id!!,
                schoolName = opponentSchool.schoolName,
                teamName = opponentSchool.teamName,
                schoolLogoUrl = opponentSchool.schoolLogoUrl
            )
        }
    }
}
