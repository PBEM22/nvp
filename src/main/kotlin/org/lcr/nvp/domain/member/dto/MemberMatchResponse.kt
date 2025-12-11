package org.lcr.nvp.domain.member.dto

import io.swagger.v3.oas.annotations.media.Schema
import org.lcr.nvp.domain.match.domain.Match
import java.time.LocalDate

@Schema(description = "회원의 참여 경기 목록 응답 DTO")
data class MemberMatchResponse(
    @Schema(description = "경기 ID")
    val matchId: Long,
    @Schema(description = "경기 날짜")
    val matchDate: LocalDate?,
    @Schema(description = "대회 이름")
    val tournamentName: String,
    @Schema(description = "상대팀 이름 (학교 포함)")
    val opponentDisplayName: String,
    @Schema(description = "승리 여부")
    val isWin: Boolean,
    @Schema(description = "우리 팀 세트 스코어")
    val teamScore: Int,
    @Schema(description = "상대 팀 세트 스코어")
    val opponentScore: Int
) {
    companion object {
        fun from(match: Match): MemberMatchResponse {
            return MemberMatchResponse(
                matchId = match.id!!,
                matchDate = match.matchDate,
                tournamentName = match.tournament.tournamentName,
                opponentDisplayName = "${match.opponentSchool.schoolName} (${match.opponentSchool.teamName})",
                isWin = match.isWin,
                teamScore = match.teamScore,
                opponentScore = match.opponentScore
            )
        }
    }
}
