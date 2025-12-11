package org.lcr.nvp.domain.member.application

import org.lcr.nvp.domain.member.dto.AssignmentHistoryDto
import org.lcr.nvp.domain.member.dto.MemberDetailResponse
import org.lcr.nvp.domain.member.repository.MemberAssignmentRepository
import org.lcr.nvp.domain.member.repository.MemberRepository
import org.lcr.nvp.domain.member.repository.UserRepository
import org.lcr.nvp.global.exception.domain.MemberNotFoundException
import org.lcr.nvp.global.exception.domain.UserNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class MemberService(
    private val userRepository: UserRepository,
    private val memberRepository: MemberRepository,
    private val memberAssignmentRepository: MemberAssignmentRepository
) {

    fun getMyInfo(userEmail: String): MemberDetailResponse {
        val user = userRepository.findByEmail(userEmail)
            ?: throw UserNotFoundException()

        val member = memberRepository.findByUser(user)
            ?: throw MemberNotFoundException() // 정식 회원이 아닌 경우

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
            assignments = assignmentHistoryDtos
        )
    }
}
