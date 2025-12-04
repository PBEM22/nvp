package org.lcr.nvp.domain.comment.dto

import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime

// ===== Request DTOs =====

data class CreateCommentRequest(
    @field:NotBlank(message = "댓글 내용은 비어있을 수 없습니다.")
    val content: String,
    val parentId: Long? = null // 대댓글이 아닌 경우 null
)

data class UpdateCommentRequest(
    @field:NotBlank(message = "댓글 내용은 비어있을 수 없습니다.")
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
