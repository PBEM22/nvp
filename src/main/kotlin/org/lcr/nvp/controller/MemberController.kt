package org.lcr.nvp.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.lcr.nvp.domain.attendance.application.AttendanceService
import org.lcr.nvp.domain.attendance.dto.GroupedMyAttendanceResponse
import org.lcr.nvp.domain.match.application.MatchService
import org.lcr.nvp.domain.match.application.ScoreRecordService
import org.lcr.nvp.domain.match.application.TournamentService
import org.lcr.nvp.domain.match.dto.ScoreRecordResponse
import org.lcr.nvp.domain.match.dto.TournamentResponse
import org.lcr.nvp.domain.member.application.MemberService
import org.lcr.nvp.domain.member.dto.MemberDetailResponse
import org.lcr.nvp.domain.member.dto.MemberInfoResponse
import org.lcr.nvp.domain.member.dto.MemberMatchResponse
import org.lcr.nvp.domain.member.dto.MemberSearchFilter
import org.lcr.nvp.domain.member.dto.UpdateMyInfoRequest
import org.lcr.nvp.global.common.ApiResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.security.Principal

@Tag(name = "회원 API", description = "회원 관련 정보 조회 API")
@RestController
@RequestMapping("/api/v1/members")
class MemberController(
    private val memberService: MemberService,
    private val attendanceService: AttendanceService,
    private val scoreRecordService: ScoreRecordService,
    private val matchService: MatchService,
    private val tournamentService: TournamentService
) {

    @Operation(
        summary = "전체 회원 목록 조회 (공개용)",
        description = "필터링/검색/정렬/페이징을 지원하는 공개용 회원 목록 조회 API입니다. <br>" +
                "정렬(sort) 예시: `?sort=user.name,asc` (이름 가나다순), `?sort=id,desc` (최신 가입순)"
    )
    @GetMapping
    fun getMembers(
        @Parameter(description = "활동 연도") @RequestParam(required = false) year: Int?,
        @Parameter(description = "기수") @RequestParam(required = false) periodNumber: Int?,
        @Parameter(description = "부서 ID") @RequestParam(required = false) departmentId: Long?,
        @Parameter(description = "직책 ID") @RequestParam(required = false) positionId: Long?,
        @Parameter(description = "검색어 (이름 또는 학과)") @RequestParam(required = false) keyword: String?,
        @PageableDefault(sort = ["id"], direction = Sort.Direction.DESC) pageable: Pageable
    ): ResponseEntity<ApiResponse<Page<MemberInfoResponse>>> {
        val filter = MemberSearchFilter(
            year = year,
            periodNumber = periodNumber,
            departmentId = departmentId,
            positionId = positionId,
            keyword = keyword
        )
        val members = memberService.getMembers(filter, pageable)
        return ResponseEntity.ok(ApiResponse.onSuccess(members))
    }

    @Operation(summary = "내 정보 상세 조회", description = "로그인된 사용자의 상세 정보와 역대 활동 이력을 조회합니다.")
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    fun getMyInfo(principal: Principal): ResponseEntity<ApiResponse<MemberDetailResponse>> {
        val myInfo = memberService.getMyInfo(principal.name)
        return ResponseEntity.ok(ApiResponse.onSuccess(myInfo))
    }

    @Operation(summary = "내 정보 수정", description = "로그인된 사용자의 개인 정보(이름, 생일, 학과 등)를 수정합니다.")
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    fun updateMyInfo(
        principal: Principal,
        @RequestBody request: UpdateMyInfoRequest
    ): ResponseEntity<ApiResponse<Unit>> {
        memberService.updateMyInfo(principal.name, request)
        return ResponseEntity.ok(ApiResponse.onSuccess())
    }

    @Operation(summary = "회원 탈퇴", description = "로그인된 사용자 본인의 계정을 탈퇴(비활성) 처리합니다.")
    @DeleteMapping("/me")
    @PreAuthorize("isAuthenticated()")
    fun withdraw(principal: Principal): ResponseEntity<ApiResponse<Unit>> {
        memberService.withdrawMember(principal.name)
        return ResponseEntity.ok(ApiResponse.onSuccess())
    }

    @Operation(summary = "내 출석률 및 상세 내역 조회 (기수별 그룹)", description = "로그인된 사용자의 출석 내역을 기수별로 그룹화하여 조회합니다.")
    @GetMapping("/me/attendance")
    @PreAuthorize("isAuthenticated()")
    fun getMyAttendance(principal: Principal): ResponseEntity<ApiResponse<GroupedMyAttendanceResponse>> {
        val myAttendance = attendanceService.getMyAttendance(principal.name)
        return ResponseEntity.ok(ApiResponse.onSuccess(myAttendance))
    }

    @Operation(summary = "내 통산 기록 조회 (마이페이지용)", description = "로그인된 사용자 본인의 통산 기록을 조회합니다.")
    @GetMapping("/me/score-record")
    @PreAuthorize("isAuthenticated()")
    fun getMyScoreRecord(principal: Principal): ResponseEntity<ApiResponse<ScoreRecordResponse>> {
        val scoreRecord = scoreRecordService.getMyScoreRecord(principal.name)
        val response = ScoreRecordResponse.from(scoreRecord)
        return ResponseEntity.ok(ApiResponse.onSuccess(response))
    }

    @Operation(summary = "내 참여 대회 목록 조회", description = "로그인된 사용자 본인이 참여한 대회의 목록을 조회합니다.")
    @GetMapping("/me/tournaments")
    @PreAuthorize("isAuthenticated()")
    fun getMyTournaments(principal: Principal): ResponseEntity<ApiResponse<List<TournamentResponse>>> {
        val tournaments = tournamentService.getMyParticipatedTournaments(principal.name)
            .map { TournamentResponse.from(it) }
        return ResponseEntity.ok(ApiResponse.onSuccess(tournaments))
    }

    @Operation(summary = "특정 회원 통산 기록 조회 (공개용)", description = "특정 회원의 모든 경기 기록을 합산한 통산 스탯(성공률, 효율 포함)을 조회합니다.")
    @GetMapping("/{memberId}/score-record")
    fun getMemberScoreRecord(
        @Parameter(description = "조회할 회원의 ID") @PathVariable memberId: Long
    ): ResponseEntity<ApiResponse<ScoreRecordResponse>> {
        val scoreRecord = scoreRecordService.getScoreRecordByMember(memberId)
        val response = ScoreRecordResponse.from(scoreRecord)
        return ResponseEntity.ok(ApiResponse.onSuccess(response))
    }

    @Operation(summary = "특정 회원 참여 경기 목록 조회 (공개용)", description = "특정 회원이 참여한 모든 경기의 목록을 조회합니다.")
    @GetMapping("/{memberId}/matches")
    fun getMemberMatches(
        @Parameter(description = "조회할 회원의 ID") @PathVariable memberId: Long
    ): ResponseEntity<ApiResponse<List<MemberMatchResponse>>> {
        val matches = matchService.getMatchesByMember(memberId)
        return ResponseEntity.ok(ApiResponse.onSuccess(matches))
    }

    @Operation(summary = "특정 회원 참여 대회 목록 조회 (공개용)", description = "특정 회원이 참여한 모든 대회의 목록을 조회합니다.")
    @GetMapping("/{memberId}/tournaments")
    fun getMemberTournaments(
        @Parameter(description = "조회할 회원의 ID") @PathVariable memberId: Long
    ): ResponseEntity<ApiResponse<List<TournamentResponse>>> {
        val tournaments = tournamentService.getTournamentsByMemberId(memberId)
            .map { TournamentResponse.from(it) }
        return ResponseEntity.ok(ApiResponse.onSuccess(tournaments))
    }

    @Operation(summary = "특정 회원 상세 정보 조회 (공개용)", description = "특정 회원의 상세 정보를 조회합니다.")
    @GetMapping("/{memberId}")
    fun getMemberInfo(
        @Parameter(description = "조회할 회원의 ID") @PathVariable memberId: Long
    ): ResponseEntity<ApiResponse<MemberDetailResponse>> {
        val memberInfo = memberService.getMemberInfo(memberId)
        return ResponseEntity.ok(ApiResponse.onSuccess(memberInfo))
    }
}
