package org.lcr.nvp.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.lcr.nvp.domain.match.application.MatchService
import org.lcr.nvp.domain.match.dto.MatchCreateRequest
import org.lcr.nvp.domain.match.dto.MatchResultUpdateRequest
import org.lcr.nvp.global.common.ApiResponse
import org.lcr.nvp.global.common.dto.CreatedResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.lcr.nvp.domain.match.dto.MatchResponse // 응답 DTO 추가
import org.springframework.web.bind.annotation.PathVariable

@Tag(name = "경기 관리 API", description = "경기 생성, 조회, 수정 등 관리를 위한 API")
@RestController
@RequestMapping("/api/v1/matches")
class MatchController(
    private val matchService: MatchService
) {

    @Operation(summary = "경기 생성", description = "새로운 경기를 시스템에 등록합니다.")
    @PostMapping
    fun createMatch(@Valid @RequestBody request: MatchCreateRequest): ResponseEntity<ApiResponse<CreatedResponse>> {
        val savedMatch = matchService.createMatch(request)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.onSuccess(CreatedResponse(id = savedMatch.id!!)))
    }

    @Operation(summary = "경기 결과 업데이트", description = "특정 경기의 승패 및 최종 세트 스코어를 업데이트합니다.")
    @PutMapping("/{matchId}/result")
    fun updateMatchResult(
        @PathVariable matchId: Long,
        @Valid @RequestBody request: MatchResultUpdateRequest
    ): ResponseEntity<ApiResponse<MatchResponse>> {
        val updatedMatch = matchService.updateMatchResult(matchId, request)
        return ResponseEntity.ok(ApiResponse.onSuccess(MatchResponse.from(updatedMatch)))
    }
}
