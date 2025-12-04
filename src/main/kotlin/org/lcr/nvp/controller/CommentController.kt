package org.lcr.nvp.controller

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

@RestController
@RequestMapping("/api")
class CommentController(
    private val commentService: CommentService
) {

    @PostMapping("/boards/{boardId}/comments")
    @PreAuthorize("isAuthenticated()")
    fun createComment(
        @PathVariable boardId: Long,
        @RequestBody request: CreateCommentRequest,
        principal: Principal
    ): ResponseEntity<ApiResponse<CreatedResponse>> {
        val authorEmail = principal.name
        val savedComment = commentService.createComment(boardId, authorEmail, request)
        val response = CreatedResponse(id = savedComment.id)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.onSuccess(response))
    }

    @GetMapping("/boards/{boardId}/comments")
    @PreAuthorize("isAuthenticated()")
    fun getComments(@PathVariable boardId: Long): ResponseEntity<ApiResponse<List<CommentResponse>>> {
        val comments = commentService.getCommentsByBoard(boardId)
        return ResponseEntity.ok(ApiResponse.onSuccess(comments))
    }

    @PutMapping("/comments/{commentId}")
    @PreAuthorize("isAuthenticated()")
    fun updateComment(
        @PathVariable commentId: Long,
        @RequestBody request: UpdateCommentRequest,
        principal: Principal
    ): ResponseEntity<ApiResponse<Unit>> {
        val userEmail = principal.name
        commentService.updateComment(commentId, userEmail, request)
        return ResponseEntity.ok(ApiResponse.onSuccess())
    }

    @DeleteMapping("/comments/{commentId}")
    @PreAuthorize("isAuthenticated()")
    fun deleteComment(
        @PathVariable commentId: Long,
        principal: Principal
    ): ResponseEntity<ApiResponse<Unit>> {
        val userEmail = principal.name
        commentService.deleteComment(commentId, userEmail)
        return ResponseEntity.ok(ApiResponse.onSuccess())
    }
}
