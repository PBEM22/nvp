package org.lcr.nvp.domain.match.dto

import io.swagger.v3.oas.annotations.media.Schema
import org.lcr.nvp.domain.match.domain.MatchRecord
import org.lcr.nvp.domain.member.domain.Member
import kotlin.math.roundToInt

@Schema(description = "단일 경기에 대한 한 선수의 요약 기록 응답 DTO")
data class MatchPlayerSummaryResponse(
    @Schema(description = "선수 고유 ID", example = "1")
    val memberId: Long,
    @Schema(description = "선수 이름", example = "김선수")
    val playerName: String,
    @Schema(description = "등번호", example = "10")
    val backNumber: Int?,
    @Schema(description = "해당 경기에서 출전한 총 세트 수", example = "3")
    val setsPlayed: Int,
    @Schema(description = "해당 경기 총 득점", example = "15")
    val totalScore: Int,

    // 주요 성공 횟수
    @Schema(description = "공격 성공 수")
    val attackSuccess: Int,
    @Schema(description = "디그 성공 수")
    val digSuccess: Int,
    @Schema(description = "블로킹 성공 수")
    val blockSuccess: Int,

    // 주요 성공률 및 효율
    @Schema(description = "공격 성공률 (%)")
    val attackSuccessRate: Double,
    @Schema(description = "공격 효율 (%)")
    val attackEfficiency: Double,
    @Schema(description = "리시브 성공률 (%)")
    val receiveSuccessRate: Double,
    @Schema(description = "리시브 효율 (%)")
    val receiveEfficiency: Double,
    @Schema(description = "블로킹 세트당 평균")
    val blockAvgPerSet: Double,
    @Schema(description = "서브 성공률 (%)")
    val serveSuccessRate: Double
) {
    companion object {
        private fun Double.roundTo(decimals: Int): Double {
            val multiplier = Math.pow(10.0, decimals.toDouble())
            return (this * multiplier).roundToInt() / multiplier
        }

        private fun calculateRate(success: Int, attempt: Int): Double {
            if (attempt == 0) return 0.0
            return (success.toDouble() / attempt * 100).roundTo(2)
        }

        private fun calculateEfficiency(numerator: Int, denominator: Int): Double {
            if (denominator == 0) return 0.0
            return (numerator.toDouble() / denominator * 100).roundTo(2)
        }

        private fun calculateAvgPerSet(total: Int, sets: Int): Double {
            if (sets == 0) return 0.0
            return (total.toDouble() / sets).roundTo(2)
        }

        fun from(member: Member, records: List<MatchRecord>): MatchPlayerSummaryResponse {
            // Raw 데이터 집계
            val setsPlayed = records.size
            val totalScore = records.sumOf { it.score }
            val totalAttackAttempt = records.sumOf { it.attackAttempt }
            val totalAttackSuccess = records.sumOf { it.attackSuccess }
            val totalAttackError = records.sumOf { it.attackError }
            val totalAttackBlock = records.sumOf { it.attackBlock }
            val totalReceiveAttempt = records.sumOf { it.receiveAttempt }
            val totalReceivePerfect = records.sumOf { it.receivePerfect }
            val totalReceiveError = records.sumOf { it.receiveError }
            val totalBlockSuccess = records.sumOf { it.blockSuccess }
            val totalServeAttempt = records.sumOf { it.serveAttempt }
            val totalServeError = records.sumOf { it.serveError }
            val totalDigSuccess = records.sumOf { it.digSuccess }


            return MatchPlayerSummaryResponse(
                memberId = member.id!!,
                playerName = member.user.name!!,
                backNumber = member.backNumber,
                setsPlayed = setsPlayed,
                totalScore = totalScore,

                attackSuccess = totalAttackSuccess,
                digSuccess = totalDigSuccess,
                blockSuccess = totalBlockSuccess,

                attackSuccessRate = calculateRate(totalAttackSuccess, totalAttackAttempt),
                attackEfficiency = calculateEfficiency(totalAttackSuccess - totalAttackError - totalAttackBlock, totalAttackAttempt),
                receiveSuccessRate = calculateRate(totalReceivePerfect, totalReceiveAttempt),
                receiveEfficiency = calculateEfficiency(totalReceivePerfect - totalReceiveError, totalReceiveAttempt),
                blockAvgPerSet = calculateAvgPerSet(totalBlockSuccess, setsPlayed),
                serveSuccessRate = calculateRate(totalServeAttempt - totalServeError, totalServeAttempt)
            )
        }
    }
}
