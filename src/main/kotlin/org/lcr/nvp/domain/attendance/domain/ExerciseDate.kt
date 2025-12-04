package org.lcr.nvp.domain.attendance.domain

import jakarta.persistence.*
import org.lcr.nvp.global.common.BaseTimeEntity
import java.time.LocalDate

@Entity
@Table(name = "exercise_dates")
class ExerciseDate(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exercise_date_id")
    val id: Long = 0,

    @Column(name = "exercise_date", nullable = false, unique = true)
    val date: LocalDate

) : BaseTimeEntity()
