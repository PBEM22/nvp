package org.lcr.nvp.domain.member.repository

import org.lcr.nvp.domain.member.domain.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface MemberAssignmentRepository : JpaRepository<MemberAssignment, Long> {

    @Query("SELECT COUNT(ma) > 0 FROM MemberAssignment ma WHERE ma.member = :member AND ma.department = :department AND ma.position = :position AND ma.period = :period")
    fun existsWithDetails(
        @Param("member") member: Member,
        @Param("department") department: Department,
        @Param("position") position: Position,
        @Param("period") period: Period
    ): Boolean

    fun existsByDepartment(department: Department): Boolean
    fun existsByPosition(position: Position): Boolean

    /**
     * 특정 회원의 모든 직책 할당 이력을 기간(연도, 학기)의 내림차순으로 조회합니다.
     */
    @Query("SELECT ma FROM MemberAssignment ma JOIN FETCH ma.department JOIN FETCH ma.position JOIN FETCH ma.period WHERE ma.member = :member ORDER BY ma.period.year DESC, ma.period.semester DESC")
    fun findAllByMemberWithDetails(@Param("member") member: Member): List<MemberAssignment>

    @Query("SELECT ma FROM MemberAssignment ma JOIN FETCH ma.member JOIN FETCH ma.member.user WHERE ma.period = :period ORDER BY ma.member.user.name ASC")
    fun findAllByPeriodWithMember(@Param("period") period: Period): List<MemberAssignment>

    @Query("SELECT ma FROM MemberAssignment ma JOIN FETCH ma.department JOIN FETCH ma.position JOIN FETCH ma.period WHERE ma.member IN :members ORDER BY ma.member.id, ma.period.year DESC, ma.period.semester DESC")
    fun findAllByMemberInWithDetails(@Param("members") members: List<Member>): List<MemberAssignment>
}
