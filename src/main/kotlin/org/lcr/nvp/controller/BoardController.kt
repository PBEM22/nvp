package org.lcr.nvp.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.lcr.nvp.domain.board.application.BoardService
import org.lcr.nvp.domain.board.dto.BoardDetailResponse
import org.lcr.nvp.domain.board.dto.BoardSummaryResponse
import org.lcr.nvp.domain.board.dto.CreateBoardRequest
import org.lcr.nvp.domain.board.dto.UpdateBoardRequest
import org.lcr.nvp.domain.comment.application.CommentService
import org.lcr.nvp.domain.comment.dto.CommentResponse
import org.lcr.nvp.domain.comment.dto.CreateCommentRequest
import org.lcr.nvp.global.common.ApiResponse
import org.lcr.nvp.global.common.dto.CreatedResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.security.Principal

@Tag(name = "게시판 API", description = "게시글 및 댓글 CRUD 관련 API")
@RestController
@RequestMapping("/api/v1/boards")
class BoardController(
    private val boardService: BoardService,
    private val commentService: CommentService
) {

    @Operation(summary = "게시글 작성", description = "새로운 게시글을 등록합니다. boardType: NOTICE, PHOTO(운영진), FREE, INQUIRY(회원)")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    fun createBoard(
        @Valid @RequestBody request: CreateBoardRequest,
        principal: Principal
    ): ResponseEntity<ApiResponse<CreatedResponse>> {
        val authorEmail = principal.name
        val savedBoard = boardService.createBoard(request, authorEmail)
        val response = CreatedResponse(id = savedBoard.id)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.onSuccess(response))
    }

    @Operation(summary = "게시글 목록 조회", description = "게시글 목록을 페이징하여 조회합니다. boardType으로 필터링할 수 있습니다.")
    @GetMapping
    fun getBoardList(
        @PageableDefault(sort = ["id"], direction = Sort.Direction.DESC) pageable: Pageable,
        @Parameter(description = "게시판 타입 (e.g., NOTICE, FREE, PHOTO)") @RequestParam(required = false) boardType: String?
    ): ResponseEntity<ApiResponse<Page<BoardSummaryResponse>>> {
        val boardPage = boardService.getBoardList(pageable, boardType)
        return ResponseEntity.ok(ApiResponse.onSuccess(boardPage))
    }

    @Operation(summary = "게시글 상세 조회", description = "특정 게시글의 상세 내용을 조회합니다.")
    @GetMapping("/{boardId}")
    fun getBoardDetails(
        @Parameter(description = "조회할 게시글의 ID") @PathVariable boardId: Long
    ): ResponseEntity<ApiResponse<BoardDetailResponse>> {
        val boardDetails = boardService.getBoardDetails(boardId)
        return ResponseEntity.ok(ApiResponse.onSuccess(boardDetails))
    }

    @Operation(summary = "게시글 수정", description = "본인이 작성한 게시글의 제목과 내용을 수정합니다.")
    @PutMapping("/{boardId}")
    @PreAuthorize("isAuthenticated()")
    fun updateBoard(
        @Parameter(description = "수정할 게시글의 ID") @PathVariable boardId: Long,
        @Valid @RequestBody request: UpdateBoardRequest,
        principal: Principal
    ): ResponseEntity<ApiResponse<Unit>> {
        val userEmail = principal.name
        boardService.updateBoard(boardId, request, userEmail)
        return ResponseEntity.ok(ApiResponse.onSuccess())
    }

    @Operation(summary = "게시글 삭제", description = "본인이 작성했거나 운영진인 경우, 게시글을 삭제합니다.")
    @DeleteMapping("/{boardId}")
    @PreAuthorize("isAuthenticated()")
    fun deleteBoard(
        @Parameter(description = "삭제할 게시글의 ID") @PathVariable boardId: Long,
        principal: Principal
    ): ResponseEntity<ApiResponse<Unit>> {
        val userEmail = principal.name
        boardService.deleteBoard(boardId, userEmail)
        return ResponseEntity.ok(ApiResponse.onSuccess())
    }

    // Comments
    @Operation(summary = "댓글/대댓글 작성", description = "특정 게시글에 댓글 또는 대댓글을 작성합니다. 대댓글인 경우 parentId를 포함합니다.")
    @PostMapping("/{boardId}/comments")
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
    @GetMapping("/{boardId}/comments")
    fun getComments(
        @Parameter(description = "댓글 목록을 조회할 게시글의 ID") @PathVariable boardId: Long
    ): ResponseEntity<ApiResponse<List<CommentResponse>>> {
        val comments = commentService.getCommentsByBoard(boardId)
        return ResponseEntity.ok(ApiResponse.onSuccess(comments))
    }
}
