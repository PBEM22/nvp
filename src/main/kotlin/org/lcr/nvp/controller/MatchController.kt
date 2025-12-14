package org.lcr.nvp.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.lcr.nvp.domain.match.application.MatchService
import org.lcr.nvp.domain.match.dto.MatchCreateRequest
import org.lcr.nvp.domain.match.dto.MatchDetailResponse
import org.lcr.nvp.domain.match.dto.MatchPlayerSummaryResponse
import org.lcr.nvp.domain.match.dto.MatchResponse
import org.lcr.nvp.domain.match.dto.MatchResultUpdateRequest
import org.lcr.nvp.global.common.ApiResponse
import org.lcr.nvp.global.common.dto.CreatedResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@Tag(name = "경기 관리 API", description = "경기 생성, 조회, 수정 등 관리를 위한 API")
@RestController
@RequestMapping("/api/v1/matches")
class MatchController(
    private val matchService: MatchService
) {

    @Operation(summary = "[운영진] 경기 생성", description = "새로운 경기를 시스템에 등록합니다.")
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    fun createMatch(@Valid @RequestBody request: MatchCreateRequest): ResponseEntity<ApiResponse<CreatedResponse>> {
        val savedMatch = matchService.createMatch(request)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.onSuccess(CreatedResponse(id = savedMatch.id!!)))
    }

    @Operation(summary = "[운영진] 경기 결과 업데이트", description = "특정 경기의 승패, 최종 세트 스코어, 수상자 정보를 업데이트합니다.")
    @PutMapping("/{matchId}/result")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    fun updateMatchResult(
        @PathVariable matchId: Long,
        @Valid @RequestBody request: MatchResultUpdateRequest
    ): ResponseEntity<ApiResponse<MatchResponse>> {
        val matchResponse = matchService.updateMatchResult(matchId, request)
        return ResponseEntity.ok(ApiResponse.onSuccess(matchResponse))
    }

    @Operation(summary = "[운영진] 경기 삭제", description = "특정 경기를 삭제(비활성) 처리합니다. 경기 기록 및 통계는 보존됩니다.")
    @DeleteMapping("/{matchId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    fun deleteMatch(@PathVariable matchId: Long): ResponseEntity<ApiResponse<Unit>> {
        matchService.deleteMatch(matchId)
        return ResponseEntity.ok(ApiResponse.onSuccess())
    }

    @Operation(summary = "경기 상세 정보 조회", description = "특정 경기의 상세 정보, 수상자, 참여 선수의 경기 기록을 모두 조회합니다.")
    @GetMapping("/{matchId}/details")
    fun getMatchDetails(@PathVariable matchId: Long): ResponseEntity<ApiResponse<MatchDetailResponse>> {
        val response = matchService.getMatchDetails(matchId)
        return ResponseEntity.ok(ApiResponse.onSuccess(response))
    }

    @Operation(summary = "특정 경기 선수별 요약 기록 조회", description = "특정 경기에 참여한 모든 선수들의 핵심 스탯(총득점, 성공률 등)을 조회합니다.")
    @GetMapping("/{matchId}/records")
    fun getMatchPlayerRecords(@PathVariable matchId: Long): ResponseEntity<ApiResponse<List<MatchPlayerSummaryResponse>>> {
        val records = matchService.getMatchPlayerRecords(matchId)
        return ResponseEntity.ok(ApiResponse.onSuccess(records))
    }
}
