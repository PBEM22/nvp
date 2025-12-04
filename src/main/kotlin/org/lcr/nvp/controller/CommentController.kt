package org.lcr.nvp.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.lcr.nvp.domain.comment.application.CommentService
import org.lcr.nvp.domain.comment.dto.CommentResponse
import org.lcr.nvp.domain.comment.dto.CreateCommentRequest
import org.lcr.nvp.domain.comment.dto.UpdateCommentRequest
import org.lcr.nvp.global.common.ApiResponse
import org.lcr.nvp.global.common.dto.CreatedResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.security.Principal

@Tag(name = "댓글 API", description = "댓글/대댓글 CRUD 관련 API")
@RestController
@RequestMapping("/api")
class CommentController(
    private val commentService: CommentService
) {

    @Operation(summary = "댓글/대댓글 작성", description = "특정 게시글에 댓글 또는 대댓글을 작성합니다. 대댓글인 경우 parentId를 포함합니다.")
    @PostMapping("/boards/{boardId}/comments")
    @PreAuthorize("isAuthenticated()")
    fun createComment(
        @Parameter(description = "댓글을 작성할 게시글의 ID") @PathVariable boardId: Long,
        @Valid @RequestBody request: CreateCommentRequest,
        principal: Principal
    ): ResponseEntity<ApiResponse<CreatedResponse>> {
        val authorEmail = principal.name
        val savedComment = commentService.createComment(boardId, authorEmail, request)
        val response = CreatedResponse(id = savedComment.id)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.onSuccess(response))
    }

    @Operation(summary = "댓글 목록 조회", description = "특정 게시글의 모든 댓글을 계층 구조로 조회합니다.")
    @GetMapping("/boards/{boardId}/comments")
    @PreAuthorize("isAuthenticated()")
    fun getComments(
        @Parameter(description = "댓글 목록을 조회할 게시글의 ID") @PathVariable boardId: Long
    ): ResponseEntity<ApiResponse<List<CommentResponse>>> {
        val comments = commentService.getCommentsByBoard(boardId)
        return ResponseEntity.ok(ApiResponse.onSuccess(comments))
    }

    @Operation(summary = "댓글 수정", description = "본인이 작성한 댓글의 내용을 수정합니다.")
    @PutMapping("/comments/{commentId}")
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
    @DeleteMapping("/comments/{commentId}")
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
