package org.lcr.nvp.domain.comment.application

import org.lcr.nvp.domain.board.repository.BoardRepository
import org.lcr.nvp.domain.comment.domain.Comment
import org.lcr.nvp.domain.comment.dto.CommentResponse
import org.lcr.nvp.domain.comment.dto.CreateCommentRequest
import org.lcr.nvp.domain.comment.dto.UpdateCommentRequest
import org.lcr.nvp.domain.comment.repository.CommentRepository
import org.lcr.nvp.domain.member.repository.UserRepository
import org.lcr.nvp.global.exception.BusinessException
import org.lcr.nvp.global.exception.ErrorCode
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class CommentService(
    private val commentRepository: CommentRepository,
    private val userRepository: UserRepository,
    private val boardRepository: BoardRepository
) {

    @Transactional
    fun createComment(boardId: Long, authorEmail: String, request: CreateCommentRequest): Comment {
        val author = userRepository.findByEmail(authorEmail)
            ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)

        val board = boardRepository.findById(boardId)
            .orElseThrow { BusinessException(ErrorCode.BOARD_NOT_FOUND) }

        // 대댓글인 경우, 부모 댓글 확인
        val parentComment = request.parentId?.let {
            val p = commentRepository.findById(it)
                .orElseThrow { BusinessException(ErrorCode.COMMENT_NOT_FOUND) }

            // 부모 댓글이 같은 게시글에 속해있는지 확인
            if (p.board.id != boardId) {
                throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)
            }
            p
        }

        val comment = Comment(
            board = board,
            author = author,
            parent = parentComment,
            content = request.content
        )

        return commentRepository.save(comment)
    }

    fun getCommentsByBoard(boardId: Long): List<CommentResponse> {
        val board = boardRepository.findById(boardId)
            .orElseThrow { BusinessException(ErrorCode.BOARD_NOT_FOUND) }

        // 현재 사용자의 역할 확인
        val authentication = SecurityContextHolder.getContext().authentication
        val hasManagerRole = authentication?.authorities?.any { it.authority in listOf("ROLE_MANAGER", "ROLE_ADMIN") } ?: false

        val comments = commentRepository.findByBoardWithAuthor(board)
        val commentResponseMap = mutableMapOf<Long, CommentResponse>()
        val rootComments = mutableListOf<CommentResponse>()

        comments.forEach { comment ->
            val content = if (comment.deletedAt != null) {
                if (hasManagerRole) "[삭제됨] ${comment.content}" else "삭제된 댓글입니다."
            } else {
                comment.content
            }

            val responseDto = CommentResponse(
                commentId = comment.id,
                authorName = comment.author.name,
                content = content,
                createdAt = comment.createdAt,
                isDeleted = comment.deletedAt != null
            )
            commentResponseMap[comment.id] = responseDto

            if (comment.parent != null) {
                commentResponseMap[comment.parent!!.id]?.children?.add(responseDto)
            } else {
                rootComments.add(responseDto)
            }
        }
        
        // 계층 구조를 만든 후, 최종적으로 정렬 (루트 댓글과 자식 댓글 모두)
        rootComments.sortBy { it.createdAt }
        rootComments.forEach { sortChildren(it) }

        return rootComments
    }

    private fun sortChildren(commentResponse: CommentResponse) {
        commentResponse.children.sortBy { it.createdAt }
        commentResponse.children.forEach { sortChildren(it) }
    }

    @Transactional
    fun updateComment(commentId: Long, userEmail: String, request: UpdateCommentRequest) {
        val comment = commentRepository.findById(commentId)
            .orElseThrow { BusinessException(ErrorCode.COMMENT_NOT_FOUND) }

        if (comment.author.email != userEmail) {
            throw BusinessException(ErrorCode.FORBIDDEN)
        }

        comment.content = request.content
    }

    @Transactional
    fun deleteComment(commentId: Long, userEmail: String) {
        val comment = commentRepository.findById(commentId)
            .orElseThrow { BusinessException(ErrorCode.COMMENT_NOT_FOUND) }

        val currentUser = userRepository.findByEmail(userEmail)
            ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)

        val hasManagerRole = currentUser.roles.any { it.roleName in listOf("ROLE_MANAGER", "ROLE_ADMIN") }

        // 작성자 본인이거나 운영진만 삭제 가능
        if (comment.author.email != userEmail && !hasManagerRole) {
            throw BusinessException(ErrorCode.FORBIDDEN)
        }

        comment.softDelete() // BaseTimeEntity의 메소드를 호출하여 deleted_at에 시간만 기록
    }
}
