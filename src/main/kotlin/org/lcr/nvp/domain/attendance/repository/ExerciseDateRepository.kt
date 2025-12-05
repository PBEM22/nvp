package org.lcr.nvp.domain.attendance.repository

import org.lcr.nvp.domain.attendance.domain.ExerciseDate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface ExerciseDateRepository : JpaRepository<ExerciseDate, Long> {
    fun findByDate(date: LocalDate): ExerciseDate?
    fun findAllByOrderByDateDesc(): List<ExerciseDate>
}
