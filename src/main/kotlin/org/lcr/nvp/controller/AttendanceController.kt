package org.lcr.nvp.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.lcr.nvp.domain.attendance.application.AttendanceService
import org.lcr.nvp.domain.attendance.dto.CheckInRequest
import org.lcr.nvp.domain.attendance.dto.DailyAttendanceStatusResponse
import org.lcr.nvp.domain.attendance.dto.GenerateCodeRequest
import org.lcr.nvp.domain.attendance.dto.GenerateCodeResponse
import org.lcr.nvp.domain.attendance.dto.TodayAttendanceResponse
import org.lcr.nvp.global.common.ApiResponse
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.time.LocalDate

@Tag(name = "출석 API", description = "출석 코드 생성 및 체크인 관련 API")
@RestController
@RequestMapping("/api")
class AttendanceController(
    private val attendanceService: AttendanceService
) {

    @Operation(
        summary = "[운영진] 특정 날짜 출석 현황 조회",
        description = "특정 날짜의 출석 상태를 조회합니다. periodId 파라미터로 특정 기수를 지정할 수 있으며, 없으면 현재 활동 기수를 기준으로 조회합니다."
    )
    @GetMapping("/admin/attendance/{date}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    fun getDailyAttendance(
        @Parameter(description = "조회할 날짜", example = "2024-05-10") @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate,
        @Parameter(description = "조회할 기수의 ID (선택 사항)") @RequestParam(required = false) periodId: Long?
    ): ResponseEntity<ApiResponse<List<DailyAttendanceStatusResponse>>> {
        val response = attendanceService.getDailyAttendanceStatus(date, periodId)
        return ResponseEntity.ok(ApiResponse.onSuccess(response))
    }

    @Operation(summary = "[운영진] 출석 코드 생성", description = "특정 회차의 출석 코드를 생성하고 10분간 활성화합니다. 기존 코드가 있으면 덮어씁니다.")
    @PostMapping("/admin/attendance/code")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    fun generateAttendanceCode(
        @Valid @RequestBody request: GenerateCodeRequest
    ): ResponseEntity<ApiResponse<GenerateCodeResponse>> {
        val response = attendanceService.generateAttendanceCode(request.round)
        return ResponseEntity.ok(ApiResponse.onSuccess(response))
    }

    @Operation(summary = "[운영진] 출석 코드 즉시 만료", description = "현재 활성화된 출석 코드를 즉시 비활성화합니다.")
    @DeleteMapping("/admin/attendance/code")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    fun invalidateAttendanceCode(): ResponseEntity<ApiResponse<Unit>> {
        attendanceService.invalidateAttendanceCode()
        return ResponseEntity.ok(ApiResponse.onSuccess())
    }

    @Operation(summary = "[운영진] 출석 상태 수동 변경", description = "특정 회원의 특정 날짜, 특정 회차의 출석 상태를 강제로 변경합니다.")
    @PutMapping("/admin/attendance/status")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    fun updateAttendanceStatus(
        @Valid @RequestBody request: org.lcr.nvp.domain.attendance.dto.UpdateAttendanceRequest
    ): ResponseEntity<ApiResponse<Unit>> {
        attendanceService.updateAttendanceStatus(request)
        return ResponseEntity.ok(ApiResponse.onSuccess())
    }

    @Operation(summary = "[회원] 출석 체크인", description = "발급된 출석 코드로 출석 체크를 합니다.")
    @PostMapping("/attendance/check-in")
    @PreAuthorize("isAuthenticated()")
    fun checkIn(
        @Valid @RequestBody request: CheckInRequest,
        principal: Principal
    ): ResponseEntity<ApiResponse<Unit>> {
        attendanceService.checkIn(principal.name, request)
        return ResponseEntity.ok(ApiResponse.onSuccess())
    }

    @Operation(summary = "[회원] 오늘의 내 출석 상태 조회", description = "로그인한 사용자의 오늘 날짜 출석 상태(1차, 2차)를 조회합니다.")
    @GetMapping("/v1/attendance/today")
    @PreAuthorize("isAuthenticated()")
    fun getTodayAttendance(principal: Principal): ResponseEntity<ApiResponse<TodayAttendanceResponse>> {
        val response = attendanceService.getTodayAttendance(principal.name)
        return ResponseEntity.ok(ApiResponse.onSuccess(response))
    }
}
