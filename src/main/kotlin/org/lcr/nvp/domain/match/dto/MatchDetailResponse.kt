package org.lcr.nvp.domain.match.dto

import io.swagger.v3.oas.annotations.media.Schema
import org.lcr.nvp.domain.match.domain.Match
import org.lcr.nvp.domain.member.domain.Member
import java.time.LocalDate

@Schema(description = "경기 상세 정보 응답 DTO (경기 정보, 수상자, 참여선수 경기기록 포함)")
data class MatchDetailResponse(
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
    val opponentScore: Int,

    @Schema(description = "해당 경기 수상자 정보")
    val awards: MatchAwardsResponse,

    @Schema(description = "해당 경기에 참여한 선수들의 경기 기록 목록")
    val playerMatchStats: List<MatchPlayerSummaryResponse>
) {
    companion object {
        fun from(
            match: Match,
            awardMembers: Map<Long, Member>,
            playerMatchStats: List<MatchPlayerSummaryResponse>
        ): MatchDetailResponse {
            return MatchDetailResponse(
                matchId = match.id!!,
                matchDate = match.matchDate,
                tournamentName = match.tournament.tournamentName,
                opponentDisplayName = "${match.opponentSchool.schoolName} (${match.opponentSchool.teamName})",
                isWin = match.isWin,
                teamScore = match.teamScore,
                opponentScore = match.opponentScore,
                awards = MatchAwardsResponse.from(match, awardMembers),
                playerMatchStats = playerMatchStats
            )
        }
    }
}
