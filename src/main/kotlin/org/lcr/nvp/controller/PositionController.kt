package org.lcr.nvp.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.lcr.nvp.domain.member.application.PositionService
import org.lcr.nvp.domain.member.dto.CreatePositionRequest
import org.lcr.nvp.domain.member.dto.PositionResponse
import org.lcr.nvp.domain.member.dto.UpdatePositionRequest
import org.lcr.nvp.global.common.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@Tag(name = "운영진 직책 관리 API", description = "운영진이 직책(Position)을 관리하는 API")
@RestController
@RequestMapping("/api/admin/positions")
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
class PositionController(
    private val positionService: PositionService
) {

    @Operation(summary = "전체 직책 목록 조회", description = "시스템에 등록된 모든 직책 목록을 조회합니다.")
    @GetMapping
    fun getPositions(): ResponseEntity<ApiResponse<List<PositionResponse>>> {
        val positions = positionService.getPositions()
        return ResponseEntity.ok(ApiResponse.onSuccess(positions))
    }

    @Operation(summary = "직책 생성", description = "새로운 직책을 시스템에 등록합니다.")
    @PostMapping
    fun createPosition(@Valid @RequestBody request: CreatePositionRequest): ResponseEntity<ApiResponse<PositionResponse>> {
        val position = positionService.createPosition(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.onSuccess(position))
    }

    @Operation(summary = "직책 수정", description = "특정 직책의 이름을 수정합니다.")
    @PutMapping("/{id}")
    fun updatePosition(@PathVariable id: Long, @Valid @RequestBody request: UpdatePositionRequest): ResponseEntity<ApiResponse<PositionResponse>> {
        val position = positionService.updatePosition(id, request)
        return ResponseEntity.ok(ApiResponse.onSuccess(position))
    }

    @Operation(summary = "직책 삭제", description = "특정 직책을 삭제합니다. 해당 직책을 가진 회원이 없어야 삭제 가능합니다.")
    @DeleteMapping("/{id}")
    fun deletePosition(@PathVariable id: Long): ResponseEntity<ApiResponse<Unit>> {
        positionService.deletePosition(id)
        return ResponseEntity.ok(ApiResponse.onSuccess())
    }
}
