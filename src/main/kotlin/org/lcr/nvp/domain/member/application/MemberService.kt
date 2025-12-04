package org.lcr.nvp.domain.member.application

import org.lcr.nvp.domain.member.dto.AssignmentHistoryDto
import org.lcr.nvp.domain.member.dto.MemberDetailResponse
import org.lcr.nvp.domain.member.repository.MemberAssignmentRepository
import org.lcr.nvp.domain.member.repository.MemberRepository
import org.lcr.nvp.domain.member.repository.UserRepository
import org.lcr.nvp.global.exception.BusinessException
import org.lcr.nvp.global.exception.ErrorCode
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
            ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)

        val member = memberRepository.findByUser(user)
            ?: throw BusinessException(ErrorCode.MEMBER_NOT_FOUND) // 정식 회원이 아닌 경우

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
            profileImageUrl = member.profileImageUrl,
            backNumber = member.backNumber,
            isPublic = member.isPublic,
            membershipStatus = member.membershipStatus,
            assignments = assignmentHistoryDtos
        )
    }
}
