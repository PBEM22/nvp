package org.lcr.nvp.domain.match.domain

import jakarta.persistence.*

@Entity
@Table(name = "tournaments")
class Tournament(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tournament_id")
    val id: Long? = null,

    @Column(nullable = false)
    val tournamentName: String,

    @Column(nullable = false)
    val isSixPlayer: Boolean = false
)
