package org.lcr.nvp.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.lcr.nvp.domain.member.application.MemberAdminService
import org.lcr.nvp.domain.member.dto.AssignPositionRequest
import org.lcr.nvp.domain.member.dto.MemberDetailResponse
import org.lcr.nvp.domain.member.dto.MemberSummaryResponse
import org.lcr.nvp.domain.member.dto.UpdateMemberStatusRequest
import org.lcr.nvp.domain.member.dto.UpdateUserRolesRequest
import org.lcr.nvp.global.common.ApiResponse
import org.lcr.nvp.global.common.dto.CreatedResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@Tag(name = "운영진 회원 관리 API", description = "운영진이 회원을 관리하는 API")
@RestController
@RequestMapping("/api/admin")
class MemberAdminController(
    private val memberAdminService: MemberAdminService
) {

    @Operation(summary = "엑셀 파일로 회원 일괄 등록/수정", description = "엑셀 파일을 업로드하여 다수의 회원을 시스템에 등록하거나 활동 기수/직책을 업데이트합니다.")
    @PostMapping("/members/upload", consumes = ["multipart/form-data"])
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    fun uploadMembersExcel(
        @Parameter(description = "업로드할 엑셀 파일") @RequestParam("file") file: MultipartFile
    ): ResponseEntity<ApiResponse<String>> {
        val resultSummary = memberAdminService.processMemberExcel(file)
        return ResponseEntity.ok(ApiResponse.onSuccess(resultSummary))
    }

    @Operation(summary = "회원 등록용 엑셀 템플릿 다운로드", description = "회원 일괄 등록에 사용되는 엑셀 템플릿 파일을 다운로드합니다.")
    @GetMapping("/members/upload/template")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    fun downloadExcelTemplate(): ResponseEntity<ByteArray> {
        val excelBytes = memberAdminService.downloadExcelTemplate()

        val headers = org.springframework.http.HttpHeaders()
        headers.add(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=NVP_MemberFile_EX.xlsx")

        return ResponseEntity.ok()
            .headers(headers)
            .contentType(org.springframework.http.MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(excelBytes)
    }

    @Operation(
        summary = "전체 회원 목록 조회",
        description = "페이징 처리된 전체 회원 목록을 조회합니다. <br> " +
                "정렬(sort) 파라미터 사용 예시: ?sort=id,desc (최신순) 또는 ?sort=name,asc (이름 가나다순)"
    )
    @GetMapping("/members")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    fun getAllMembers(
        @PageableDefault(sort = ["id"], direction = Sort.Direction.DESC) pageable: Pageable
    ): ResponseEntity<ApiResponse<Page<MemberSummaryResponse>>> {
        val members = memberAdminService.getAllMembers(pageable)
        return ResponseEntity.ok(ApiResponse.onSuccess(members))
    }

    @Operation(summary = "특정 회원 상세 정보 조회", description = "특정 회원의 상세 정보와 직책 이력을 조회합니다.")
    @GetMapping("/members/{memberId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    fun getMemberDetails(
        @Parameter(description = "조회할 회원의 memberId") @PathVariable memberId: Long
    ): ResponseEntity<ApiResponse<MemberDetailResponse>> {
        val memberDetails = memberAdminService.getMemberDetails(memberId)
        return ResponseEntity.ok(ApiResponse.onSuccess(memberDetails))
    }

    @Operation(summary = "회원 자격 상태 변경", description = "특정 회원의 자격 상태를 변경합니다. (e.g., ACTIVE_MEMBER, ALUMNI)")
    @PutMapping("/members/{memberId}/status")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    fun updateMemberStatus(
        @Parameter(description = "상태를 변경할 회원의 memberId") @PathVariable memberId: Long,
        @Valid @RequestBody request: UpdateMemberStatusRequest
    ): ResponseEntity<ApiResponse<Unit>> {
        memberAdminService.updateMemberStatus(memberId, request.membershipStatus)
        return ResponseEntity.ok(ApiResponse.onSuccess())
    }

    @Operation(summary = "[ADMIN] 사용자 역할 변경", description = "특정 사용자의 역할을 지정된 목록으로 변경합니다. (예: ROLE_MANAGER로 승격)")
    @PutMapping("/users/{userId}/roles")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun updateUserRoles(
        @Parameter(description = "역할을 변경할 사용자의 userId") @PathVariable userId: Long,
        @Valid @RequestBody request: UpdateUserRolesRequest
    ): ResponseEntity<ApiResponse<Unit>> {
        memberAdminService.updateUserRoles(userId, request.roles)
        return ResponseEntity.ok(ApiResponse.onSuccess())
    }

    @Operation(summary = "정식 회원으로 등록", description = "일반 사용자(User)를 정식 회원(Member)으로 등록하고 ROLE_MEMBER 역할을 부여합니다.")
    @PostMapping("/users/{userId}/promotion")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    fun promoteToMember(
        @Parameter(description = "정식 회원으로 등록할 사용자의 userId") @PathVariable userId: Long
    ): ResponseEntity<ApiResponse<CreatedResponse>> {
        val savedMember = memberAdminService.promoteToMember(userId)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.onSuccess(CreatedResponse(id = savedMember.id)))
    }

    @Operation(summary = "회원 직책 할당", description = "특정 회원에게 특정 기간의 부서 및 직책을 할당합니다.")
    @PostMapping("/users/{userId}/assignments")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    fun assignPosition(
        @Parameter(description = "직책을 할당할 사용자의 userId") @PathVariable userId: Long,
        @Valid @RequestBody request: AssignPositionRequest
    ): ResponseEntity<ApiResponse<CreatedResponse>> {
        val savedAssignment = memberAdminService.assignPosition(userId, request)
        val response = CreatedResponse(id = savedAssignment.id)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.onSuccess(response))
    }
}
