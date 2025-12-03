package org.lcr.nvp.domain.board.repository

import org.lcr.nvp.domain.board.domain.Board
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface BoardRepository : JpaRepository<Board, Long> {

    @Query("SELECT b FROM Board b WHERE b.boardType NOT IN :boardTypes AND b.deletedAt IS NULL")
    fun findAllByBoardTypeNotIn(
        @Param("boardTypes") boardTypes: Collection<String>,
        pageable: Pageable
    ): Page<Board>

    // [사용자용] 상세 조회 시 삭제 안 된 것만 찾기 위한 헬퍼 메서드
    fun findByIdAndDeletedAtIsNull(id: Long): Optional<Board>

    // [사용자용] 삭제 안 된 것만 조회 (WHERE deleted_at IS NULL)
    @Query("SELECT b FROM Board b WHERE b.boardType = :type AND b.deletedAt IS NULL")
    fun findAllActiveBoards(type: String, pageable: Pageable): Page<Board>

    // [관리자용] 삭제된 것도 포함해서 모두 조회 (조건 없음)
    @Query("SELECT b FROM Board b WHERE b.boardType = :type")
    fun findAllBoardsForAdmin(type: String, pageable: Pageable): Page<Board>

    // [관리자용] 삭제된 글만 따로 보기 (휴지통 기능)
    @Query("SELECT b FROM Board b WHERE b.deletedAt IS NOT NULL")
    fun findDeletedBoards(pageable: Pageable): Page<Board>
}
