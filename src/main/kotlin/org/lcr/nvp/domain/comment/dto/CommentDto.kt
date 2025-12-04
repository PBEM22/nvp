package org.lcr.nvp.domain.comment.dto

import java.time.LocalDateTime

// ===== Request DTOs =====

data class CreateCommentRequest(
    val content: String,
    val parentId: Long? = null // 대댓글이 아닌 경우 null
)

data class UpdateCommentRequest(
    val content: String
)


// ===== Response DTOs =====

data class CommentResponse(
    val commentId: Long,
    val authorName: String,
    val content: String,
    val createdAt: LocalDateTime,
    val isDeleted: Boolean,
    val children: MutableList<CommentResponse> = mutableListOf()
)
