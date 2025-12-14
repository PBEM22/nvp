package org.lcr.nvp.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.lcr.nvp.domain.attendance.application.AttendanceService
import org.lcr.nvp.domain.attendance.dto.CheckInRequest
import org.lcr.nvp.domain.attendance.dto.TodayAttendanceResponse
import org.lcr.nvp.global.common.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.security.Principal

@Tag(name = "출석 API", description = "회원용 출석 관련 API")
@RestController
@RequestMapping("/api/v1/attendance")
class AttendanceController(
    private val attendanceService: AttendanceService
) {

    @Operation(summary = "[회원] 출석 체크인", description = "발급된 출석 코드로 출석 체크를 합니다.")
    @PostMapping("/check-in")
    @PreAuthorize("isAuthenticated()")
    fun checkIn(
        @Valid @RequestBody request: CheckInRequest,
        principal: Principal
    ): ResponseEntity<ApiResponse<Unit>> {
        attendanceService.checkIn(principal.name, request)
        return ResponseEntity.ok(ApiResponse.onSuccess())
    }

    @Operation(summary = "[회원] 오늘의 내 출석 상태 조회", description = "로그인한 사용자의 오늘 날짜 출석 상태(1차, 2차)를 조회합니다.")
    @GetMapping("/today")
    @PreAuthorize("isAuthenticated()")
    fun getTodayAttendance(principal: Principal): ResponseEntity<ApiResponse<TodayAttendanceResponse>> {
        val response = attendanceService.getTodayAttendance(principal.name)
        return ResponseEntity.ok(ApiResponse.onSuccess(response))
    }
}
