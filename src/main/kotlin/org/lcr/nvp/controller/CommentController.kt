package org.lcr.nvp.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.lcr.nvp.domain.comment.application.CommentService
import org.lcr.nvp.domain.comment.dto.UpdateCommentRequest
import org.lcr.nvp.global.common.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.security.Principal

@Tag(name = "댓글 API", description = "댓글/대댓글 CRUD 관련 API")
@RestController
@RequestMapping("/api/v1/comments")
class CommentController(
    private val commentService: CommentService
) {

    @Operation(summary = "댓글 수정", description = "본인이 작성한 댓글의 내용을 수정합니다.")
    @PutMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    fun updateComment(
        @Parameter(description = "수정할 댓글의 ID") @PathVariable commentId: Long,
        @Valid @RequestBody request: UpdateCommentRequest,
        principal: Principal
    ): ResponseEntity<ApiResponse<Unit>> {
        val userEmail = principal.name
        commentService.updateComment(commentId, userEmail, request)
        return ResponseEntity.ok(ApiResponse.onSuccess())
    }

    @Operation(summary = "댓글 삭제", description = "본인이 작성했거나 운영진인 경우, 댓글을 삭제합니다.")
    @DeleteMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    fun deleteComment(
        @Parameter(description = "삭제할 댓글의 ID") @PathVariable commentId: Long,
        principal: Principal
    ): ResponseEntity<ApiResponse<Unit>> {
        val userEmail = principal.name
        commentService.deleteComment(commentId, userEmail)
        return ResponseEntity.ok(ApiResponse.onSuccess())
    }
}
