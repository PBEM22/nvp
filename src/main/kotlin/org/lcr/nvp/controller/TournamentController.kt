package org.lcr.nvp.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.lcr.nvp.domain.match.application.MatchService
import org.lcr.nvp.domain.match.application.TournamentService
import org.lcr.nvp.domain.match.dto.TournamentCreateRequest
import org.lcr.nvp.domain.match.dto.TournamentResponse
import org.lcr.nvp.domain.member.dto.MemberMatchResponse
import org.lcr.nvp.global.common.ApiResponse
import org.lcr.nvp.global.common.dto.CreatedResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.security.Principal

@Tag(name = "대회 관리 API", description = "대회 생성, 조회, 수정 등 관리를 위한 API")
@RestController
@RequestMapping("/api/v1/tournaments")
class TournamentController(
    private val tournamentService: TournamentService,
    private val matchService: MatchService
) {

    @Operation(summary = "[운영진] 대회 생성")
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    fun createTournament(@Valid @RequestBody request: TournamentCreateRequest): ResponseEntity<ApiResponse<CreatedResponse>> {
        val savedTournament = tournamentService.createTournament(request)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.onSuccess(CreatedResponse(id = savedTournament.id!!)))
    }

    @Operation(summary = "모든 대회 목록 조회")
    @GetMapping
    fun getAllTournaments(): ResponseEntity<ApiResponse<List<TournamentResponse>>> {
        val tournaments = tournamentService.getAllTournaments().map { TournamentResponse.from(it) }
        return ResponseEntity.ok(ApiResponse.onSuccess(tournaments))
    }

    @Operation(summary = "내(로그인한 사용자)가 참여한 대회 목록 조회")
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    fun getMyParticipatedTournaments(principal: Principal): ResponseEntity<ApiResponse<List<TournamentResponse>>> {
        val tournaments = tournamentService.getMyParticipatedTournaments(principal.name)
            .map { TournamentResponse.from(it) }
        return ResponseEntity.ok(ApiResponse.onSuccess(tournaments))
    }

    @Operation(summary = "특정 대회 정보 조회")
    @GetMapping("/{tournamentId}")
    fun getTournamentById(@PathVariable tournamentId: Long): ResponseEntity<ApiResponse<TournamentResponse>> {
        val tournament = tournamentService.getTournamentById(tournamentId)
        return ResponseEntity.ok(ApiResponse.onSuccess(TournamentResponse.from(tournament)))
    }

    @Operation(summary = "특정 대회에 속한 경기 목록 조회", description = "특정 대회의 모든 경기 목록을 최신순으로 조회합니다.")
    @GetMapping("/{tournamentId}/matches")
    fun getMatchesByTournament(
        @Parameter(description = "경기를 조회할 대회의 ID") @PathVariable tournamentId: Long
    ): ResponseEntity<ApiResponse<List<MemberMatchResponse>>> {
        val matches = matchService.getMatchesByTournament(tournamentId)
        return ResponseEntity.ok(ApiResponse.onSuccess(matches))
    }

    @Operation(summary = "[운영진] 대회 정보 수정")
    @PutMapping("/{tournamentId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    fun updateTournament(
        @PathVariable tournamentId: Long,
        @Valid @RequestBody request: TournamentCreateRequest
    ): ResponseEntity<ApiResponse<TournamentResponse>> {
        val updatedTournament = tournamentService.updateTournament(tournamentId, request)
        return ResponseEntity.ok(ApiResponse.onSuccess(TournamentResponse.from(updatedTournament)))
    }

    @Operation(summary = "[운영진] 대회 삭제")
    @DeleteMapping("/{tournamentId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    fun deleteTournament(@PathVariable tournamentId: Long): ResponseEntity<ApiResponse<Unit>> {
        tournamentService.deleteTournament(tournamentId)
        return ResponseEntity.ok(ApiResponse.onSuccess())
    }
}
