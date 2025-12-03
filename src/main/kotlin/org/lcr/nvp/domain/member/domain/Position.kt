package org.lcr.nvp.domain.member.domain

import jakarta.persistence.*

@Entity
@Table(name = "positions")
class Position(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "position_id")
    val id: Long = 0,

    @Column(name = "position_name", nullable = false, unique = true)
    val name: String
)
