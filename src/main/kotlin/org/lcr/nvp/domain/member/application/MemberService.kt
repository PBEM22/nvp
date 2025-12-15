package org.lcr.nvp.domain.member.application

import org.lcr.nvp.domain.member.dto.*
import org.lcr.nvp.domain.member.repository.MemberAssignmentRepository
import org.lcr.nvp.domain.member.repository.MemberRepository
import org.lcr.nvp.domain.member.repository.UserRepository
import org.lcr.nvp.global.exception.domain.InvalidInputValueException
import org.lcr.nvp.global.exception.domain.MemberNotFoundException
import org.lcr.nvp.global.exception.domain.UserNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class MemberService(
    private val userRepository: UserRepository,
    private val memberRepository: MemberRepository,
    private val memberAssignmentRepository: MemberAssignmentRepository
) {

    fun getMembers(filter: MemberSearchFilter, pageable: Pageable): Page<MemberInfoResponse> {
        val memberPage = memberRepository.findByCriteria(filter, pageable)
        val members = memberPage.content

        if (members.isEmpty()) {
            return Page.empty(pageable)
        }

        // 한 번의 쿼리로 모든 회원의 활동 이력을 가져옴 (회원ID, 기간순으로 정렬되어 있음)
        val assignments = memberAssignmentRepository.findAllByMemberInWithDetails(members)
        // 각 회원 ID별로 활동 이력 목록을 그룹화
        val assignmentsByMemberId = assignments.groupBy { it.member.id }

        return memberPage.map { member ->
            // 정렬된 목록에서 첫 번째(가장 최신) 활동 이력을 가져옴
            val latestAssignment = assignmentsByMemberId[member.id]?.firstOrNull()
            MemberInfoResponse(
                memberId = member.id,
                name = member.user.name,
                email = member.user.email,
                backNumber = member.backNumber,
                major = member.major,
                membershipStatus = member.membershipStatus,
                periodNumber = latestAssignment?.period?.periodNumber,
                periodYear = latestAssignment?.period?.year,
                departmentName = latestAssignment?.department?.name,
                positionName = latestAssignment?.position?.name,
                displayName = latestAssignment?.displayName
            )
        }
    }

    @Transactional
    fun updateMyInfo(userEmail: String, request: UpdateMyInfoRequest) {
        val user = userRepository.findByProviderId(userEmail)
            ?: userRepository.findByEmail(userEmail)
            ?: throw UserNotFoundException()

        val member = memberRepository.findByUser(user)
            ?: throw MemberNotFoundException()

        request.name?.let { user.name = it }
        request.birthday?.let { user.birthday = it }
        request.gender?.let {
            user.isMale = when (it) {
                "남성" -> true
                "여성" -> false
                else -> throw InvalidInputValueException()
            }
        }
        request.backNumber?.let { member.backNumber = it }
        request.major?.let { member.major = it }
        request.isPublic?.let { member.isPublic = it }
    }

    @Transactional
    fun withdrawMember(userEmail: String) {
        val user = userRepository.findByProviderId(userEmail)
            ?: userRepository.findByEmail(userEmail)
            ?: throw UserNotFoundException()

        val member = memberRepository.findByUser(user)
            ?: throw MemberNotFoundException()

        // User와 Member를 soft-delete 처리
        user.softDelete()
        member.softDelete()
        member.membershipStatus = "WITHDRAWN" // 상태를 '탈퇴'로 명확히 변경
    }

    fun getMyInfo(userEmail: String): MemberDetailResponse {
        val user = userRepository.findByProviderId(userEmail)
            ?: userRepository.findByEmail(userEmail)
            ?: throw UserNotFoundException()

        val member = memberRepository.findByUser(user)
        val roles = user.roles.map { it.roleName }

        // 정식 회원이 아닌 경우, User 정보만으로 응답 생성
        if (member == null) {
            return MemberDetailResponse(
                memberId = null,
                userId = user.id,
                email = user.email,
                name = user.name,
                birthday = user.birthday,
                isMale = user.isMale,
                profileImageUrl = null,
                backNumber = null,
                major = null,
                isPublic = false,
                membershipStatus = "NON_MEMBER",
                roles = roles,
                assignments = emptyList()
            )
        }

        // 정식 회원인 경우, 모든 정보 포함하여 응답 생성
        val assignments = memberAssignmentRepository.findAllByMemberWithDetails(member)
        val assignmentHistoryDtos = assignments.map { assignment ->
            AssignmentHistoryDto(
                departmentName = assignment.department.name,
                positionName = assignment.position.name,
                displayName = assignment.displayName,
                periodYear = assignment.period.year,
                periodSemester = assignment.period.semester,
                periodNumber = assignment.period.periodNumber
            )
        }

        return MemberDetailResponse(
            memberId = member.id,
            userId = user.id,
            email = user.email,
            name = user.name,
            birthday = user.birthday,
            isMale = user.isMale,
            profileImageUrl = member.profileImageUrl,
            backNumber = member.backNumber,
            major = member.major,
            isPublic = member.isPublic,
            membershipStatus = member.membershipStatus,
            roles = roles,
            assignments = assignmentHistoryDtos
        )
    }

    fun getMemberInfo(memberId: Long): MemberDetailResponse {
        val member = memberRepository.findById(memberId)
            .orElseThrow { MemberNotFoundException() }

        val assignments = memberAssignmentRepository.findAllByMemberWithDetails(member)

        val assignmentHistoryDtos = assignments.map { assignment ->
            AssignmentHistoryDto(
                departmentName = assignment.department.name,
                positionName = assignment.position.name,
                displayName = assignment.displayName,
                periodYear = assignment.period.year,
                periodSemester = assignment.period.semester,
                periodNumber = assignment.period.periodNumber
            )
        }

        val roles = member.user.roles.map { it.roleName }

        return MemberDetailResponse(
            memberId = member.id,
            userId = member.user.id,
            email = member.user.email,
            name = member.user.name,
            birthday = member.user.birthday,
            isMale = member.user.isMale,
            profileImageUrl = member.profileImageUrl,
            backNumber = member.backNumber,
            major = member.major,
            isPublic = member.isPublic,
            membershipStatus = member.membershipStatus,
            roles = roles,
            assignments = assignmentHistoryDtos
        )
    }
}
