package org.lcr.nvp.domain.member.domain

import jakarta.persistence.*

@Entity
@Table(name = "departments")
class Department(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dept_id")
    val id: Long = 0,

    @Column(name = "dept_name", nullable = false, unique = true)
    val name: String
)
