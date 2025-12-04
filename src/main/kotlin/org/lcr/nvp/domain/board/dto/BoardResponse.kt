package org.lcr.nvp.domain.board.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

/**
 * 게시글 목록 조회를 위한 DTO
 */
@Schema(description = "게시글 목록 응답을 위한 요약 DTO")
data class BoardSummaryResponse(
    @Schema(description = "게시글 ID", example = "1")
    val id: Long,
    @Schema(description = "게시판 타입", example = "FREE")
    val boardType: String,
    @Schema(description = "게시글 제목", example = "게시글 제목입니다.")
    val title: String,
    @Schema(description = "작성자 이름", example = "홍길동")
    val authorName: String,
    @Schema(description = "작성일시")
    val createdAt: LocalDateTime
)

/**
 * 게시글 상세 조회를 위한 DTO
 */
@Schema(description = "게시글 상세 정보 응답 DTO")
data class BoardDetailResponse(
    @Schema(description = "게시글 ID", example = "1")
    val id: Long,
    @Schema(description = "게시판 타입", example = "FREE")
    val boardType: String,
    @Schema(description = "게시글 제목", example = "게시글 제목입니다.")
    val title: String,
    @Schema(description = "게시글 내용", example = "게시글 내용입니다.")
    val content: String,
    @Schema(description = "작성자 이름", example = "홍길동")
    val authorName: String,
    @Schema(description = "작성일시")
    val createdAt: LocalDateTime,
    @Schema(description = "최종 수정일시")
    val updatedAt: LocalDateTime
    // TODO: 댓글 기능 구현 시, 댓글 목록(List<CommentResponse>) 추가
)
