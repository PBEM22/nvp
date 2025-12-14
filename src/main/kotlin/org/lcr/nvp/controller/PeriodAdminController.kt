package org.lcr.nvp.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.lcr.nvp.domain.member.application.PeriodService
import org.lcr.nvp.domain.member.dto.CreatePeriodRequest
import org.lcr.nvp.domain.member.dto.PeriodResponse
import org.lcr.nvp.domain.member.dto.UpdatePeriodRequest
import org.lcr.nvp.global.common.ApiResponse
import org.lcr.nvp.global.common.dto.CreatedResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@Tag(name = "운영진 기수 관리 API", description = "운영진이 기수(Period)를 관리하는 API")
@RestController
@RequestMapping("/api/admin/periods")
@PreAuthorize("hasRole('ROLE_ADMIN')") // 이 컨트롤러의 모든 기능은 총괄 관리자(ADMIN)만 가능
class PeriodAdminController(
    private val periodService: PeriodService
) {

    @Operation(summary = "[ADMIN] 기수 생성", description = "새로운 기수 정보를 시스템에 등록합니다.")
    @PostMapping
    fun createPeriod(@Valid @RequestBody request: CreatePeriodRequest): ResponseEntity<ApiResponse<CreatedResponse>> {
        val savedPeriod = periodService.createPeriod(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.onSuccess(CreatedResponse(id = savedPeriod.id)))
    }

    @Operation(summary = "[ADMIN] 기수 정보 수정", description = "특정 기수의 정보를 수정합니다.")
    @PutMapping("/{periodId}")
    fun updatePeriod(
        @PathVariable periodId: Long,
        @Valid @RequestBody request: UpdatePeriodRequest
    ): ResponseEntity<ApiResponse<Unit>> {
        periodService.updatePeriod(periodId, request)
        return ResponseEntity.ok(ApiResponse.onSuccess())
    }

    @Operation(summary = "[ADMIN] 기수 삭제", description = "특정 기수를 삭제합니다. 해당 기수에 소속된 회원이 없어야 삭제 가능합니다.")
    @DeleteMapping("/{periodId}")
    fun deletePeriod(@PathVariable periodId: Long): ResponseEntity<ApiResponse<Unit>> {
        periodService.deletePeriod(periodId)
        return ResponseEntity.ok(ApiResponse.onSuccess())
    }

    @Operation(summary = "[ADMIN] 현재 활동 기수 설정", description = "특정 기수를 현재 활동 기수로 설정합니다. 기존의 현재 기수는 자동으로 해제됩니다.")
    @PutMapping("/{periodId}/set-current")
    fun setCurrentPeriod(@PathVariable periodId: Long): ResponseEntity<ApiResponse<Unit>> {
        periodService.setCurrentPeriod(periodId)
        return ResponseEntity.ok(ApiResponse.onSuccess())
    }
}
