package org.lcr.nvp.domain.member.domain

import jakarta.persistence.*

@Entity
@Table(name = "roles")
class Role(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    val id: Long = 0,

    @Column(name = "role_name", nullable = false, unique = true)
    val roleName: String
)
