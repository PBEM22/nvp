package org.lcr.nvp.domain.match.domain

import jakarta.persistence.*

@Entity
@Table(name = "opponent_schools")
class OpponentSchool(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "opponent_school_id")
    val id: Long? = null,

    @Column(nullable = false)
    val schoolName: String,

    @Column(nullable = false)
    val teamName: String,

    val schoolLogoUrl: String? = null
)
