package org.lcr.nvp.domain.board.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "게시글 생성 요청 DTO")
data class CreateBoardRequest(
    @Schema(description = "게시판 타입", example = "FREE")
    val boardType: String,
    @Schema(description = "게시글 제목", example = "새로운 게시글 제목입니다.")
    val title: String,
    @Schema(description = "게시글 내용", example = "게시글 내용입니다. 여기에 글을 작성합니다.")
    val content: String
)

@Schema(description = "게시글 수정 요청 DTO")
data class UpdateBoardRequest(
    @Schema(description = "수정할 게시글 제목", example = "수정된 제목입니다.")
    val title: String,
    @Schema(description = "수정할 게시글 내용", example = "내용이 수정되었습니다.")
    val content: String
)
