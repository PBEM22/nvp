package org.lcr.nvp.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.lcr.nvp.domain.member.application.RoleMappingService
import org.lcr.nvp.domain.member.dto.CreateRoleMappingRequest
import org.lcr.nvp.domain.member.dto.RoleMappingResponse
import org.lcr.nvp.domain.member.dto.UpdateRoleMappingRequest
import org.lcr.nvp.global.common.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@Tag(name = "운영진 역할 매핑 관리 API", description = "운영진이 '부서+직책' 조합의 표시 이름을 관리하는 API")
@RestController
@RequestMapping("/api/admin/role-mappings")
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
class RoleMappingController(
    private val roleMappingService: RoleMappingService
) {

    @Operation(summary = "전체 역할 매핑 목록 조회")
    @GetMapping
    fun getAllMappings(): ResponseEntity<ApiResponse<List<RoleMappingResponse>>> {
        val mappings = roleMappingService.getAllMappings()
        return ResponseEntity.ok(ApiResponse.onSuccess(mappings))
    }

    @Operation(summary = "역할 매핑 생성")
    @PostMapping
    fun createMapping(@Valid @RequestBody request: CreateRoleMappingRequest): ResponseEntity<ApiResponse<RoleMappingResponse>> {
        val mapping = roleMappingService.createMapping(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.onSuccess(mapping))
    }

    @Operation(summary = "역할 매핑 수정")
    @PutMapping("/{id}")
    fun updateMapping(@PathVariable id: Long, @Valid @RequestBody request: UpdateRoleMappingRequest): ResponseEntity<ApiResponse<RoleMappingResponse>> {
        val mapping = roleMappingService.updateMapping(id, request)
        return ResponseEntity.ok(ApiResponse.onSuccess(mapping))
    }

    @Operation(summary = "역할 매핑 삭제")
    @DeleteMapping("/{id}")
    fun deleteMapping(@PathVariable id: Long): ResponseEntity<ApiResponse<Unit>> {
        roleMappingService.deleteMapping(id)
        return ResponseEntity.ok(ApiResponse.onSuccess())
    }
}
