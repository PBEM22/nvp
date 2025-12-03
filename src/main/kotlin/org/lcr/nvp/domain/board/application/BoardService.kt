package org.lcr.nvp.domain.board.application

import org.lcr.nvp.domain.board.domain.Board
import org.lcr.nvp.domain.board.dto.BoardDetailResponse
import org.lcr.nvp.domain.board.dto.BoardSummaryResponse
import org.lcr.nvp.domain.board.dto.CreateBoardRequest
import org.lcr.nvp.domain.board.dto.UpdateBoardRequest
import org.lcr.nvp.domain.board.repository.BoardRepository
import org.lcr.nvp.domain.member.repository.UserRepository
import org.lcr.nvp.global.exception.BusinessException
import org.lcr.nvp.global.exception.ErrorCode
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class BoardService(
    private val boardRepository: BoardRepository,
    private val userRepository: UserRepository
) {

    fun getBoardList(pageable: Pageable, boardType: String?): Page<BoardSummaryResponse> {
        val boardPage = if (boardType != null) {
            // 문의게시판은 타입 지정 필터링으로 조회 불가
            if (boardType == "INQUIRY") {
                throw BusinessException(ErrorCode.FORBIDDEN)
            }
            boardRepository.findAllActiveBoards(boardType, pageable)
        } else {
            // boardType 파라미터가 없으면 INQUIRY를 제외한 모든 게시글 조회
            boardRepository.findAllByBoardTypeNotIn(listOf("INQUIRY"), pageable)
        }

        return boardPage.map { board ->
            BoardSummaryResponse(
                id = board.id,
                boardType = board.boardType,
                title = board.title,
                authorName = board.author.name,
                createdAt = board.createdAt
            )
        }
    }

    fun getBoardDetails(boardId: Long): BoardDetailResponse {
        val board = boardRepository.findByIdAndDeletedAtIsNull(boardId)
            .orElseThrow { BusinessException(ErrorCode.BOARD_NOT_FOUND) }

        // 문의게시판일 경우, 본인 또는 운영진만 조회 가능
        if (board.boardType == "INQUIRY") {
            val authentication = SecurityContextHolder.getContext().authentication
                ?: throw BusinessException(ErrorCode.FORBIDDEN) // 인증 정보가 없으면 접근 불가

            val currentUserEmail = authentication.name
            val authorEmail = board.author.email
            val currentUser = userRepository.findByEmail(currentUserEmail)
                ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)
            val hasManagerRole = currentUser.roles.any { it.roleName == "ROLE_MANAGER" || it.roleName == "ROLE_ADMIN" }

            if (currentUserEmail != authorEmail && !hasManagerRole) {
                throw BusinessException(ErrorCode.FORBIDDEN)
            }
        }

        return BoardDetailResponse(
            id = board.id,
            boardType = board.boardType,
            title = board.title,
            content = board.content,
            authorName = board.author.name,
            createdAt = board.createdAt,
            updatedAt = board.updatedAt
        )
    }


    @Transactional
    fun createBoard(request: CreateBoardRequest, authorEmail: String): Board {
        val author = userRepository.findByEmail(authorEmail)
            ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)

        // 게시판 타입별 쓰기 권한 체크
        when (request.boardType) {
            "NOTICE", "PHOTO" -> {
                val hasManagerRole = author.roles.any { it.roleName == "ROLE_MANAGER" || it.roleName == "ROLE_ADMIN" }
                if (!hasManagerRole) {
                    throw BusinessException(ErrorCode.FORBIDDEN)
                }
            }
            "FREE", "INQUIRY" -> {
                // 이 게시판들은 로그인한 사용자라면 누구나 작성 가능하므로 별도의 역할 검사를 하지 않음.
                // 컨트롤러에서 @PreAuthorize("isAuthenticated()")로 인증 여부를 이미 확인했음.
            }
            else -> throw BusinessException(ErrorCode.INVALID_INPUT_VALUE) // 유효하지 않은 게시판 타입
        }

        val board = Board(
            boardType = request.boardType,
            title = request.title,
            content = request.content,
            author = author
        )

        return boardRepository.save(board)
    }

    @Transactional
    fun updateBoard(boardId: Long, request: UpdateBoardRequest, userEmail: String) {
        // 삭제된 글(deletedAt != null)을 수정하려 할 때 BOARD_NOT_FOUND 예외가 터짐
        val board = boardRepository.findByIdAndDeletedAtIsNull(boardId)
            .orElseThrow { BusinessException(ErrorCode.BOARD_NOT_FOUND) }

        // 작성자 본인 확인
        if (board.author.email != userEmail) {
            throw BusinessException(ErrorCode.FORBIDDEN)
        }

        board.title = request.title
        board.content = request.content

    }

    @Transactional
    fun deleteBoard(boardId: Long, userEmail: String) {
        val board = boardRepository.findByIdAndDeletedAtIsNull(boardId)
            .orElseThrow { BusinessException(ErrorCode.BOARD_NOT_FOUND) }

        val currentUser = userRepository.findByEmail(userEmail)
            ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)

        val hasManagerRole = currentUser.roles.any { it.roleName == "ROLE_MANAGER" || it.roleName == "ROLE_ADMIN" }

        // 작성자 본인이거나 운영진만 삭제 가능
        if (board.author.email != userEmail && !hasManagerRole) {
            throw BusinessException(ErrorCode.FORBIDDEN)
        }

        board.softDelete()
    }
}
