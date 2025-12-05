package org.lcr.nvp.domain.member.domain

import jakarta.persistence.*

@Entity
@Table(name = "periods")
class Period(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "period_id")
    val id: Long = 0,

    @Column(name = "period_year", nullable = false)
    var year: Int,

    @Column(name = "period_semester", nullable = false)
    var semester: Int,

    @Column(name = "period_number", nullable = false, unique = true)
    var periodNumber: Int,

    @Column(name = "is_current", nullable = false)
    var isCurrent: Boolean = false
)
