package org.lcr.nvp.domain.member.repository

import org.lcr.nvp.domain.member.domain.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface MemberAssignmentRepository : JpaRepository<MemberAssignment, Long> {
    fun existsByMemberAndDepartmentAndPositionAndPeriod(
        member: Member,
        department: Department,
        position: Position,
        period: Period
    ): Boolean

    /**
     * 특정 회원의 모든 직책 할당 이력을 기간(연도, 학기)의 내림차순으로 조회합니다.
     */
    @Query("SELECT ma FROM MemberAssignment ma JOIN FETCH ma.department JOIN FETCH ma.position JOIN FETCH ma.period WHERE ma.member = :member ORDER BY ma.period.year DESC, ma.period.semester DESC")
    fun findAllByMemberWithDetails(@Param("member") member: Member): List<MemberAssignment>
}
