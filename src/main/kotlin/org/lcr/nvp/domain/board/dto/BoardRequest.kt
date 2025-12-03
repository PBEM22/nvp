package org.lcr.nvp.domain.board.dto

data class CreateBoardRequest(
    val boardType: String,
    val title: String,
    val content: String
)

data class UpdateBoardRequest(
    val title: String,
    val content: String
)
