package org.lcr.nvp.domain.comment.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime

// ===== Request DTOs =====

@Schema(description = "댓글/대댓글 생성 요청 DTO")
data class CreateCommentRequest(
    @Schema(description = "댓글 내용", example = "정말 좋은 글입니다!")
    @field:NotBlank(message = "댓글 내용은 비어있을 수 없습니다.")
    val content: String,
    @Schema(description = "대댓글인 경우, 부모 댓글의 ID", example = "1", nullable = true)
    val parentId: Long? = null // 대댓글이 아닌 경우 null
)

@Schema(description = "댓글 수정 요청 DTO")
data class UpdateCommentRequest(
    @Schema(description = "수정할 댓글 내용", example = "정말 좋은 글이네요!")
    @field:NotBlank(message = "댓글 내용은 비어있을 수 없습니다.")
    val content: String
)


// ===== Response DTOs =====

@Schema(description = "댓글/대댓글 응답 DTO")
data class CommentResponse(
    @Schema(description = "댓글 ID", example = "1")
    val commentId: Long,
    @Schema(description = "작성자 이름", example = "김댓글")
    val authorName: String,
    @Schema(description = "댓글 내용", example = "좋은 글 감사합니다.")
    val content: String,
    @Schema(description = "작성일시")
    val createdAt: LocalDateTime,
    @Schema(description = "삭제 여부", example = "false")
    val isDeleted: Boolean,
    @Schema(description = "대댓글 목록")
    val children: MutableList<CommentResponse> = mutableListOf()
)
