package org.lcr.nvp.domain.comment.domain

import jakarta.persistence.*
import org.lcr.nvp.domain.board.domain.Board
import org.lcr.nvp.domain.member.domain.User
import org.lcr.nvp.global.common.BaseTimeEntity

@Entity
@Table(name = "comments")
class Comment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    val board: Board,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val author: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    var parent: Comment? = null,

    @Lob
    @Column(name = "comment_info", nullable = false)
    var content: String

) : BaseTimeEntity() {

    // 대댓글을 관리하기 위한 양방향 매핑
    @OneToMany(mappedBy = "parent", orphanRemoval = true)
    val children: MutableList<Comment> = mutableListOf()
}
