package org.lcr.nvp.domain.attendance.repository

import org.lcr.nvp.domain.attendance.domain.Attendance
import org.lcr.nvp.domain.attendance.domain.ExerciseDate
import org.lcr.nvp.domain.member.domain.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface AttendanceRepository : JpaRepository<Attendance, Long> {
    fun findByMemberAndExerciseDate(member: Member, exerciseDate: ExerciseDate): Attendance?

    @Query("SELECT a FROM Attendance a JOIN FETCH a.member JOIN FETCH a.member.user WHERE a.exerciseDate = :exerciseDate")
    fun findAllByExerciseDateWithMember(@Param("exerciseDate") exerciseDate: ExerciseDate): List<Attendance>

    @Query("SELECT a FROM Attendance a JOIN FETCH a.exerciseDate WHERE a.member = :member ORDER BY a.exerciseDate.date DESC")
    fun findByMemberWithExerciseDate(@Param("member") member: Member): List<Attendance>

    fun findAllByMemberInAndExerciseDateIn(members: List<Member>, exerciseDates: List<ExerciseDate>): List<Attendance>
}
