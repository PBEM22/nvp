package org.lcr.nvp.domain.member.domain

import jakarta.persistence.*
import org.lcr.nvp.global.common.BaseTimeEntity

@Entity
@Table(name = "members")
class Member(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    val id: Long = 0,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    val user: User,

    @Column(name = "profile_image_url", length = 1000)
    var profileImageUrl: String? = null,

    @Column(name = "back_number", nullable = true)
    var backNumber: Int? = null,

    @Column(name = "is_public", nullable = false)
    var isPublic: Boolean = false,

    @Column(name = "membership_status", nullable = false)
    var membershipStatus: String = "ACTIVE_MEMBER"

) : BaseTimeEntity()
