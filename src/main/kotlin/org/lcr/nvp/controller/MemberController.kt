package org.lcr.nvp.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.lcr.nvp.domain.member.application.MemberService
import org.lcr.nvp.domain.member.dto.MemberDetailResponse
import org.lcr.nvp.global.common.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@Tag(name = "회원 API", description = "인증된 회원 자신의 정보 관련 API")
@RestController
@RequestMapping("/api/members")
class MemberController(
    private val memberService: MemberService
) {

    @Operation(summary = "내 정보 상세 조회", description = "로그인된 사용자의 상세 정보와 역대 활동 이력을 조회합니다.")
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    fun getMyInfo(principal: Principal): ResponseEntity<ApiResponse<MemberDetailResponse>> {
        val myInfo = memberService.getMyInfo(principal.name)
        return ResponseEntity.ok(ApiResponse.onSuccess(myInfo))
    }
}
