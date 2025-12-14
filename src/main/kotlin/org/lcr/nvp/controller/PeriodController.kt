package org.lcr.nvp.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.lcr.nvp.domain.member.application.PeriodService
import org.lcr.nvp.domain.member.dto.PeriodResponse
import org.lcr.nvp.global.common.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "기수 API (공개용)", description = "기수 정보 조회를 위한 공개용 API")
@RestController
@RequestMapping("/api/v1/periods")
class PeriodController(
    private val periodService: PeriodService
) {

    @Operation(summary = "전체 기수 목록 조회", description = "시스템에 등록된 모든 기수 목록을 최신순으로 조회합니다.")
    @GetMapping
    fun getAllPeriods(): ResponseEntity<ApiResponse<List<PeriodResponse>>> {
        val periods = periodService.getAllPeriods()
        return ResponseEntity.ok(ApiResponse.onSuccess(periods))
    }

    @Operation(summary = "현재 활동 기수 정보 조회", description = "현재 활동 기수로 설정된 기수의 정보를 조회합니다.")
    @GetMapping("/current")
    fun getCurrentPeriod(): ResponseEntity<ApiResponse<PeriodResponse>> {
        val period = periodService.getCurrentPeriod()
        return ResponseEntity.ok(ApiResponse.onSuccess(period))
    }
}
