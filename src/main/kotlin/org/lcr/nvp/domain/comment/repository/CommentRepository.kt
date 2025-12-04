package org.lcr.nvp.domain.comment.repository

import org.lcr.nvp.domain.board.domain.Board
import org.lcr.nvp.domain.comment.domain.Comment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface CommentRepository : JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c JOIN FETCH c.author WHERE c.board = :board ORDER BY c.createdAt ASC")
    fun findByBoardWithAuthor(@Param("board") board: Board): List<Comment>
}
