package org.lcr.nvp.domain.member.application

import org.lcr.nvp.domain.member.domain.Member
import org.lcr.nvp.domain.member.dto.AssignPositionRequest
import org.lcr.nvp.domain.member.dto.AssignmentHistoryDto
import org.lcr.nvp.domain.member.dto.MemberDetailResponse
import org.lcr.nvp.domain.member.dto.MemberSummaryResponse
import org.lcr.nvp.domain.member.dto.PromoteMemberRequest
import org.lcr.nvp.domain.member.domain.MemberAssignment
import org.lcr.nvp.domain.member.repository.*
import org.lcr.nvp.global.exception.BusinessException
import org.lcr.nvp.global.exception.ErrorCode
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class MemberAdminService(
    private val userRepository: UserRepository,
    private val memberRepository: MemberRepository,
    private val roleRepository: RoleRepository,
    private val departmentRepository: DepartmentRepository,
    private val positionRepository: PositionRepository,
    private val periodRepository: PeriodRepository,
    private val memberAssignmentRepository: MemberAssignmentRepository
) {

    fun getAllMembers(pageable: Pageable): Page<MemberSummaryResponse> {
        return memberRepository.findAll(pageable).map { member ->
            MemberSummaryResponse(
                memberId = member.id,
                userId = member.user.id,
                email = member.user.email,
                name = member.user.name,
                membershipStatus = member.membershipStatus
            )
        }
    }

    fun getMemberDetails(memberId: Long): MemberDetailResponse {
        val member = memberRepository.findById(memberId)
            .orElseThrow { BusinessException(ErrorCode.MEMBER_NOT_FOUND) }

        val assignments = memberAssignmentRepository.findAllByMemberWithDetails(member)

        val assignmentHistoryDtos = assignments.map { assignment ->
            AssignmentHistoryDto(
                departmentName = assignment.department.name,
                positionName = assignment.position.name,
                periodYear = assignment.period.year,
                periodSemester = assignment.period.semester
            )
        }

        return MemberDetailResponse(
            memberId = member.id,
            userId = member.user.id,
            email = member.user.email,
            name = member.user.name,
            birthday = member.birthday,
            isMale = member.isMale,
            backNumber = member.backNumber,
            isPublic = member.isPublic,
            membershipStatus = member.membershipStatus,
            assignments = assignmentHistoryDtos
        )
    }

    @Transactional
    fun promoteToMember(targetUserId: Long, request: PromoteMemberRequest): Member {
        val user = userRepository.findById(targetUserId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        // 이미 Member인지 확인
        if (memberRepository.findByUser(user) != null) {
            throw BusinessException(ErrorCode.MEMBER_ALREADY_EXISTS)
        }

        // Member 생성 및 저장
        val member = Member(
            user = user,
            birthday = request.birthday,
            isMale = request.isMale
        )
        val savedMember = memberRepository.save(member)

        // ROLE_MEMBER 역할 부여
        val memberRole = roleRepository.findByRoleName("ROLE_MEMBER")
            ?: throw BusinessException(ErrorCode.ROLE_NOT_FOUND)
        user.roles.add(memberRole)
        userRepository.save(user)

        return savedMember
    }

    @Transactional
    fun updateMemberStatus(memberId: Long, newStatus: String) {
        val member = memberRepository.findById(memberId)
            .orElseThrow { BusinessException(ErrorCode.MEMBER_NOT_FOUND) }

        // TODO: newStatus가 유효한 값인지 Enum 등으로 검증하는 로직 추가 권장
        member.membershipStatus = newStatus
    }

    @Transactional
    fun assignPosition(targetUserId: Long, request: AssignPositionRequest): MemberAssignment {
        val user = userRepository.findById(targetUserId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        val member = memberRepository.findByUser(user)
            ?: throw BusinessException(ErrorCode.MEMBER_NOT_FOUND)

        val department = departmentRepository.findById(request.departmentId)
            .orElseThrow { BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND) }

        val position = positionRepository.findById(request.positionId)
            .orElseThrow { BusinessException(ErrorCode.POSITION_NOT_FOUND) }

        val period = periodRepository.findById(request.periodId)
            .orElseThrow { BusinessException(ErrorCode.PERIOD_NOT_FOUND) }

        if (memberAssignmentRepository.existsByMemberAndDepartmentAndPositionAndPeriod(
                member, department, position, period
            )
        ) {
            throw BusinessException(ErrorCode.ASSIGNMENT_DUPLICATION)
        }

        val assignment = MemberAssignment(
            member = member,
            department = department,
            position = position,
            period = period
        )

        return memberAssignmentRepository.save(assignment)
    }
}
