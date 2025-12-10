package org.lcr.nvp.domain.match.dto

import io.swagger.v3.oas.annotations.media.Schema
import org.lcr.nvp.domain.match.domain.Match
import org.lcr.nvp.domain.member.domain.Member

@Schema(description = "경기별 수상자 정보 응답 DTO")
data class MatchAwardsResponse(
    @Schema(description = "MVP 정보")
    val mvp: AwardInfo?,
    @Schema(description = "공격왕 정보")
    val bestSpiker: AwardInfo?,
    @Schema(description = "수비왕 정보")
    val bestDefender: AwardInfo?
) {
    @Schema(description = "개별 수상자 상세 정보")
    data class AwardInfo(
        @Schema(description = "수상자 memberId")
        val memberId: Long,
        @Schema(description = "수상자 이름")
        val playerName: String,
        @Schema(description = "수상자 등번호")
        val backNumber: Int?,
        @Schema(description = "선정 이유")
        val reason: String
    )

    companion object {
        fun from(match: Match, awardMembers: Map<Long, Member>): MatchAwardsResponse {
            val mvpInfo = match.mvpMemberId?.let { memberId ->
                awardMembers[memberId]?.let { member ->
                    AwardInfo(memberId, member.user.name!!, member.backNumber, match.mvpReason ?: "")
                }
            }
            val spikerInfo = match.spikerMemberId?.let { memberId ->
                awardMembers[memberId]?.let { member ->
                    AwardInfo(memberId, member.user.name!!, member.backNumber, match.spikerReason ?: "")
                }
            }
            val defenderInfo = match.defenderMemberId?.let { memberId ->
                awardMembers[memberId]?.let { member ->
                    AwardInfo(memberId, member.user.name!!, member.backNumber, match.defenderReason ?: "")
                }
            }
            
            return MatchAwardsResponse(
                mvp = mvpInfo,
                bestSpiker = spikerInfo,
                bestDefender = defenderInfo
            )
        }
    }
}
