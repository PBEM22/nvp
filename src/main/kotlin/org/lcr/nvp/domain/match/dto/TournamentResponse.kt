package org.lcr.nvp.domain.match.dto

import io.swagger.v3.oas.annotations.media.Schema
import org.lcr.nvp.domain.match.domain.Tournament

@Schema(description = "대회 정보 응답 DTO")
data class TournamentResponse(
    @Schema(description = "대회 ID", example = "1")
    val id: Long,
    @Schema(description = "대회 이름", example = "2024년 전국대학배구대회")
    val tournamentName: String,
    @Schema(description = "6인제 여부", example = "true")
    val isSixPlayer: Boolean
) {
    companion object {
        fun from(tournament: Tournament): TournamentResponse {
            return TournamentResponse(
                id = tournament.id!!,
                tournamentName = tournament.tournamentName,
                isSixPlayer = tournament.isSixPlayer
            )
        }
    }
}
