package org.lcr.nvp.domain.match.domain

import jakarta.persistence.*
import org.lcr.nvp.global.common.BaseTimeEntity

@Entity
@Table(name = "tournaments")
class Tournament(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tournament_id")
    val id: Long? = null,

    @Column(nullable = false)
    var tournamentName: String,

    @Column(nullable = false)
    var isSixPlayer: Boolean = false
) : BaseTimeEntity()
