package org.lcr.nvp.domain.member.domain

import jakarta.persistence.*
import org.lcr.nvp.global.common.BaseTimeEntity

@Entity
@Table(
    name = "member_department_positions",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_member_assignment",
            columnNames = ["member_id", "dept_id", "position_id", "period_id"]
        )
    ]
)
class MemberAssignment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dept_position_id")
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_id", nullable = false)
    val department: Department,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id", nullable = false)
    val position: Position,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "period_id", nullable = false)
    val period: Period

) : BaseTimeEntity()
