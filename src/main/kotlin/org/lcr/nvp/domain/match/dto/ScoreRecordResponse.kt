package org.lcr.nvp.domain.match.dto

import io.swagger.v3.oas.annotations.media.Schema
import org.lcr.nvp.domain.match.domain.ScoreRecord
import kotlin.math.roundToInt

@Schema(description = "선수 통산 기록 응답 DTO")
data class ScoreRecordResponse(
    @Schema(description = "선수 고유 ID", example = "1")
    val memberId: Long,
    @Schema(description = "출전한 총 대회 수", example = "2")
    val tournamentsPlayed: Int,
    @Schema(description = "출전한 총 경기 수", example = "5")
    val matchesPlayed: Int,
    @Schema(description = "출전한 총 세트 수", example = "15")
    val setsPlayed: Int,
    @Schema(description = "누적 총 득점", example = "120")
    val totalScore: Int,

    @Schema(description = "누적 총 공격 시도", example = "250")
    val totalAttackAttempt: Int,
    @Schema(description = "누적 총 공격 성공", example = "100")
    val totalAttackSuccess: Int,
    @Schema(description = "누적 총 공격 범실", example = "25")
    val totalAttackError: Int,
    @Schema(description = "누적 총 공격 차단 당함", example = "15")
    val totalAttackBlock: Int,
    @Schema(description = "통산 공격 성공률 (%)", example = "40.0")
    val attackSuccessRate: Double,
    @Schema(description = "통산 공격 효율 (%)", example = "24.0")
    val attackEfficiency: Double,

    @Schema(description = "누적 총 리시브 시도", example = "200")
    val totalReceiveAttempt: Int,
    @Schema(description = "누적 총 리시브 성공(Perfect)", example = "120")
    val totalReceivePerfect: Int,
    @Schema(description = "누적 총 리시브 범실", example = "20")
    val totalReceiveError: Int,
    @Schema(description = "통산 리시브 성공률 (%)", example = "60.0")
    val receiveSuccessRate: Double,
    @Schema(description = "통산 리시브 효율 (%)", example = "50.0")
    val receiveEfficiency: Double,

    @Schema(description = "누적 총 블로킹 시도", example = "100")
    val totalBlockAttempt: Int,
    @Schema(description = "누적 총 블로킹 성공(득점)", example = "20")
    val totalBlockSuccess: Int,
    @Schema(description = "누적 총 유효 블로킹", example = "30")
    val totalBlockEffective: Int,
    @Schema(description = "누적 총 블로킹 범실(네트터치 등)", example = "5")
    val totalBlockError: Int,
    @Schema(description = "누적 총 블로킹 실패", example = "10")
    val totalBlockFault: Int,
    @Schema(description = "통산 블로킹 성공률 (%)", example = "20.0")
    val blockSuccessRate: Double,
    @Schema(description = "통산 블로킹 효율 (%)", example = "35.0")
    val blockEfficiency: Double,

    @Schema(description = "누적 총 서브 시도", example = "150")
    val totalServeAttempt: Int,
    @Schema(description = "누적 총 서브 에이스(득점)", example = "15")
    val totalServeAce: Int,
    @Schema(description = "누적 총 서브 범실", example = "30")
    val totalServeError: Int,
    @Schema(description = "통산 서브 성공률 (%)", example = "80.0")
    val serveSuccessRate: Double,
    @Schema(description = "통산 서브 효율 (%)", example = "-10.0")
    val serveEfficiency: Double,

    @Schema(description = "누적 총 디그 시도", example = "180")
    val totalDigAttempt: Int,
    @Schema(description = "누적 총 디그 성공", example = "150")
    val totalDigSuccess: Int,
    @Schema(description = "누적 총 디그 범실", example = "10")
    val totalDigError: Int,
    @Schema(description = "통산 디그 성공률 (%)", example = "83.33")
    val digSuccessRate: Double,
    @Schema(description = "통산 디그 효율 (%)", example = "77.78")
    val digEfficiency: Double,

    @Schema(description = "누적 총 세트(토스) 시도", example = "500")
    val totalTossAttempt: Int,
    @Schema(description = "누적 총 세트 성공", example = "450")
    val totalTossSuccess: Int,
    @Schema(description = "누적 총 세트 범실", example = "20")
    val totalTossError: Int,
    @Schema(description = "통산 세트 성공률 (%)", example = "90.0")
    val tossSuccessRate: Double,
    @Schema(description = "통산 세트 효율 (%)", example = "86.0")
    val tossEfficiency: Double
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
            // 효율은 음수가 나올 수 있으므로 max(0.0, ...) 처리를 하지 않음
            return (numerator.toDouble() / denominator * 100).roundTo(2)
        }

        fun from(scoreRecord: ScoreRecord): ScoreRecordResponse {
            return ScoreRecordResponse(
                memberId = scoreRecord.member.id!!,
                tournamentsPlayed = scoreRecord.tournamentsPlayed,
                matchesPlayed = scoreRecord.matchesPlayed,
                setsPlayed = scoreRecord.setsPlayed,
                totalScore = scoreRecord.totalScore,

                totalAttackAttempt = scoreRecord.totalAttackAttempt,
                totalAttackSuccess = scoreRecord.totalAttackSuccess,
                totalAttackError = scoreRecord.totalAttackError,
                totalAttackBlock = scoreRecord.totalAttackBlock,
                attackSuccessRate = calculateRate(scoreRecord.totalAttackSuccess, scoreRecord.totalAttackAttempt),
                attackEfficiency = calculateEfficiency(scoreRecord.totalAttackSuccess - scoreRecord.totalAttackError - scoreRecord.totalAttackBlock, scoreRecord.totalAttackAttempt),

                totalReceiveAttempt = scoreRecord.totalReceiveAttempt,
                totalReceivePerfect = scoreRecord.totalReceivePerfect,
                totalReceiveError = scoreRecord.totalReceiveError,
                receiveSuccessRate = calculateRate(scoreRecord.totalReceivePerfect, scoreRecord.totalReceiveAttempt),
                receiveEfficiency = calculateEfficiency(scoreRecord.totalReceivePerfect - scoreRecord.totalReceiveError, scoreRecord.totalReceiveAttempt),

                totalBlockAttempt = scoreRecord.totalBlockAttempt,
                totalBlockSuccess = scoreRecord.totalBlockSuccess,
                totalBlockEffective = scoreRecord.totalBlockEffective,
                totalBlockError = scoreRecord.totalBlockError,
                totalBlockFault = scoreRecord.totalBlockFault,
                blockSuccessRate = calculateRate(scoreRecord.totalBlockSuccess, scoreRecord.totalBlockAttempt),
                blockEfficiency = calculateEfficiency(scoreRecord.totalBlockSuccess + scoreRecord.totalBlockEffective - scoreRecord.totalBlockError - scoreRecord.totalBlockFault, scoreRecord.totalBlockAttempt),

                totalServeAttempt = scoreRecord.totalServeAttempt,
                totalServeAce = scoreRecord.totalServeAce,
                totalServeError = scoreRecord.totalServeError,
                serveSuccessRate = calculateRate(scoreRecord.totalServeAttempt - scoreRecord.totalServeError, scoreRecord.totalServeAttempt),
                serveEfficiency = calculateEfficiency(scoreRecord.totalServeAce - scoreRecord.totalServeError, scoreRecord.totalServeAttempt),

                totalDigAttempt = scoreRecord.totalDigAttempt,
                totalDigSuccess = scoreRecord.totalDigSuccess,
                totalDigError = scoreRecord.totalDigError,
                digSuccessRate = calculateRate(scoreRecord.totalDigSuccess, scoreRecord.totalDigAttempt),
                digEfficiency = calculateEfficiency(scoreRecord.totalDigSuccess - scoreRecord.totalDigError, scoreRecord.totalDigAttempt),

                totalTossAttempt = scoreRecord.totalTossAttempt,
                totalTossSuccess = scoreRecord.totalTossSuccess,
                totalTossError = scoreRecord.totalTossError,
                tossSuccessRate = calculateRate(scoreRecord.totalTossSuccess, scoreRecord.totalTossAttempt),
                tossEfficiency = calculateEfficiency(scoreRecord.totalTossSuccess - scoreRecord.totalTossError, scoreRecord.totalTossAttempt)
            )
        }
    }
}

