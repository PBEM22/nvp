package org.lcr.nvp.domain.board.dto

import java.time.LocalDateTime

/**
 * 게시글 목록 조회를 위한 DTO
 */
data class BoardSummaryResponse(
    val id: Long,
    val boardType: String,
    val title: String,
    val authorName: String,
    val createdAt: LocalDateTime
)

/**
 * 게시글 상세 조회를 위한 DTO
 */
data class BoardDetailResponse(
    val id: Long,
    val boardType: String,
    val title: String,
    val content: String,
    val authorName: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
    // TODO: 댓글 기능 구현 시, 댓글 목록(List<CommentResponse>) 추가
)
