package org.lcr.nvp.domain.member.domain

import jakarta.persistence.*
import org.lcr.nvp.global.common.BaseTimeEntity
import java.time.LocalDate

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val email: String,

    @Column(nullable = true)
    var password: String?,

    @Column(nullable = true)
    var name: String?,

    @Column(nullable = true)
    var birthday: LocalDate?,

    @Column(name = "is_male", nullable = true)
    var isMale: Boolean?,

    @Column(name = "login_type", nullable = false)
    val loginType: String,

    @Column(unique = true)
    val providerId: String? = null,

    @Column(nullable = false)
    var status: String = "ACTIVE",

    @ManyToMany(fetch = FetchType.EAGER) // 사용자의 권한은 즉시 로딩
    @JoinTable(
        name = "member_roles",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")]
    )
    val roles: MutableSet<Role> = mutableSetOf()
) : BaseTimeEntity()
