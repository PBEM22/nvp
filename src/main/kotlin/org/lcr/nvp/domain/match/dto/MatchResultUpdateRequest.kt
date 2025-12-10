package org.lcr.nvp.domain.match.dto

import io.swagger.v3.oas.annotations.media.Schema

data class MatchResultUpdateRequest(
    @Schema(description = "경기 승리 여부")
    val isWin: Boolean,
    @Schema(description = "우리 팀의 최종 세트 스코어")
    val teamScore: Int,
    @Schema(description = "상대 팀의 최종 세트 스코어")
    val opponentScore: Int,

    @Schema(description = "MVP로 선정된 선수의 memberId")
    val mvpMemberId: Long?,
    @Schema(description = "공격왕으로 선정된 선수의 memberId")
    val spikerMemberId: Long?,
    @Schema(description = "수비왕으로 선정된 선수의 memberId")
    val defenderMemberId: Long?
)
