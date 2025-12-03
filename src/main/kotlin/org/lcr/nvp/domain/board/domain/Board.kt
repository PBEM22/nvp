package org.lcr.nvp.domain.board.domain

import jakarta.persistence.*
import org.lcr.nvp.domain.member.domain.User
import org.lcr.nvp.global.common.BaseTimeEntity

@Entity
@Table(name = "boards")
class Board(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id")
    val id: Long = 0,

    @Column(name = "board_type", nullable = false)
    val boardType: String,

    @Column(name = "board_title", nullable = false)
    var title: String,

    @Lob // TEXT 타입에 매핑, 긴 글의 경우 사용
    @Column(name = "board_info", nullable = false)
    var content: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val author: User

) : BaseTimeEntity()