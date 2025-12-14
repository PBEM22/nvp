package org.lcr.nvp.domain.attendance.repository

import org.lcr.nvp.domain.attendance.domain.ExerciseDate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface ExerciseDateRepository : JpaRepository<ExerciseDate, Long> {
    fun findByDate(date: LocalDate): ExerciseDate?
    fun findAllByOrderByDateDesc(): List<ExerciseDate>

    @Query("SELECT ed FROM ExerciseDate ed WHERE EXTRACT(YEAR FROM ed.date) = :year AND EXTRACT(MONTH FROM ed.date) BETWEEN :startMonth AND :endMonth")
    fun findByYearAndSemester(
        @Param("year") year: Int,
        @Param("startMonth") startMonth: Int,
        @Param("endMonth") endMonth: Int
    ): List<ExerciseDate>
}
