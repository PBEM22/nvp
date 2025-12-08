package org.lcr.nvp.domain.member.domain

import jakarta.persistence.*

@Entity
@Table(
    name = "role_mappings",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_role_mapping",
            columnNames = ["dept_id", "position_id"]
        )
    ]
)
class RoleMapping(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_id", nullable = false)
    val department: Department,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id", nullable = false)
    val position: Position,

    @Column(name = "display_name", nullable = false)
    var displayName: String
)
