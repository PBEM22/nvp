package org.lcr.nvp.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.lcr.nvp.domain.match.application.OpponentSchoolService
import org.lcr.nvp.domain.match.dto.OpponentSchoolCreateRequest
import org.lcr.nvp.domain.match.dto.OpponentSchoolResponse
import org.lcr.nvp.global.common.ApiResponse
import org.lcr.nvp.global.common.dto.CreatedResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "상대 학교 관리 API", description = "상대 학교 생성, 조회, 수정 등 관리를 위한 API")
@RestController
@RequestMapping("/api/v1/opponent-schools")
class OpponentSchoolController(
    private val opponentSchoolService: OpponentSchoolService
) {

    @Operation(summary = "상대 학교 생성")
    @PostMapping
    fun createOpponentSchool(@Valid @RequestBody request: OpponentSchoolCreateRequest): ResponseEntity<ApiResponse<CreatedResponse>> {
        val savedSchool = opponentSchoolService.createOpponentSchool(request)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.onSuccess(CreatedResponse(id = savedSchool.id!!)))
    }

    @Operation(summary = "모든 상대 학교 목록 조회")
    @GetMapping
    fun getAllOpponentSchools(): ResponseEntity<ApiResponse<List<OpponentSchoolResponse>>> {
        val schools = opponentSchoolService.getAllOpponentSchools().map { OpponentSchoolResponse.from(it) }
        return ResponseEntity.ok(ApiResponse.onSuccess(schools))
    }

    @Operation(summary = "특정 상대 학교 정보 조회")
    @GetMapping("/{schoolId}")
    fun getOpponentSchoolById(@PathVariable schoolId: Long): ResponseEntity<ApiResponse<OpponentSchoolResponse>> {
        val school = opponentSchoolService.getOpponentSchoolById(schoolId)
        return ResponseEntity.ok(ApiResponse.onSuccess(OpponentSchoolResponse.from(school)))
    }

    @Operation(summary = "상대 학교 정보 수정")
    @PutMapping("/{schoolId}")
    fun updateOpponentSchool(
        @PathVariable schoolId: Long,
        @Valid @RequestBody request: OpponentSchoolCreateRequest
    ): ResponseEntity<ApiResponse<OpponentSchoolResponse>> {
        val updatedSchool = opponentSchoolService.updateOpponentSchool(schoolId, request)
        return ResponseEntity.ok(ApiResponse.onSuccess(OpponentSchoolResponse.from(updatedSchool)))
    }

    @Operation(summary = "상대 학교 삭제")
    @DeleteMapping("/{schoolId}")
    fun deleteOpponentSchool(@PathVariable schoolId: Long): ResponseEntity<ApiResponse<Unit>> {
        opponentSchoolService.deleteOpponentSchool(schoolId)
        return ResponseEntity.ok(ApiResponse.onSuccess())
    }
}
