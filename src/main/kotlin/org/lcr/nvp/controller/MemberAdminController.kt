package org.lcr.nvp.controller

import org.lcr.nvp.domain.member.application.MemberAdminService
import org.lcr.nvp.domain.member.dto.*
import org.lcr.nvp.global.common.ApiResponse
import org.lcr.nvp.global.common.dto.CreatedResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin")
class MemberAdminController(
    private val memberAdminService: MemberAdminService
) {

    @GetMapping("/members")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    fun getAllMembers(pageable: Pageable): ResponseEntity<ApiResponse<Page<MemberSummaryResponse>>> {
        val members = memberAdminService.getAllMembers(pageable)
        return ResponseEntity.ok(ApiResponse.onSuccess(members))
    }

    @GetMapping("/members/{memberId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    fun getMemberDetails(@PathVariable memberId: Long): ResponseEntity<ApiResponse<MemberDetailResponse>> {
        val memberDetails = memberAdminService.getMemberDetails(memberId)
        return ResponseEntity.ok(ApiResponse.onSuccess(memberDetails))
    }

    @PutMapping("/members/{memberId}/status")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    fun updateMemberStatus(
        @PathVariable memberId: Long,
        @RequestBody request: UpdateMemberStatusRequest
    ): ResponseEntity<ApiResponse<Unit>> {
        memberAdminService.updateMemberStatus(memberId, request.membershipStatus)
        return ResponseEntity.ok(ApiResponse.onSuccess())
    }

    @PostMapping("/users/{userId}/promotion")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    fun promoteToMember(
        @PathVariable userId: Long,
        @RequestBody request: PromoteMemberRequest
    ): ResponseEntity<ApiResponse<CreatedResponse>> {
        val savedMember = memberAdminService.promoteToMember(userId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.onSuccess(CreatedResponse(id = savedMember.id)))
    }

    @PostMapping("/users/{userId}/assignments")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    fun assignPosition(
        @PathVariable userId: Long,
        @RequestBody request: AssignPositionRequest
    ): ResponseEntity<ApiResponse<CreatedResponse>> {
        val savedAssignment = memberAdminService.assignPosition(userId, request)
        val response = CreatedResponse(id = savedAssignment.id)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.onSuccess(response))
    }
}
