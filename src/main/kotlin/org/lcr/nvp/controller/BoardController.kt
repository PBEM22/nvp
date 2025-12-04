package org.lcr.nvp.controller

import org.lcr.nvp.domain.board.application.BoardService
import org.lcr.nvp.domain.board.dto.BoardDetailResponse
import org.lcr.nvp.domain.board.dto.BoardSummaryResponse
import org.lcr.nvp.domain.board.dto.CreateBoardRequest
import org.lcr.nvp.domain.board.dto.UpdateBoardRequest
import org.lcr.nvp.global.common.ApiResponse
import org.lcr.nvp.global.common.dto.CreatedResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
@RequestMapping("/api/boards")
class BoardController(
    private val boardService: BoardService
) {

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    fun createBoard(
        @RequestBody request: CreateBoardRequest,
        principal: Principal
    ): ResponseEntity<ApiResponse<CreatedResponse>> {
        val authorEmail = principal.name
        val savedBoard = boardService.createBoard(request, authorEmail)
        val response = CreatedResponse(id = savedBoard.id)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.onSuccess(response))
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    fun getBoardList(
        pageable: Pageable,
        @RequestParam(required = false) boardType: String?
    ): ResponseEntity<ApiResponse<Page<BoardSummaryResponse>>> {
        val boardPage = boardService.getBoardList(pageable, boardType)
        return ResponseEntity.ok(ApiResponse.onSuccess(boardPage))
    }

    @GetMapping("/{boardId}")
    @PreAuthorize("isAuthenticated()")
    fun getBoardDetails(@PathVariable boardId: Long): ResponseEntity<ApiResponse<BoardDetailResponse>> {
        val boardDetails = boardService.getBoardDetails(boardId)
        return ResponseEntity.ok(ApiResponse.onSuccess(boardDetails))
    }

    @PutMapping("/{boardId}")
    @PreAuthorize("isAuthenticated()")
    fun updateBoard(
        @PathVariable boardId: Long,
        @RequestBody request: UpdateBoardRequest,
        principal: Principal
    ): ResponseEntity<ApiResponse<Unit>> {
        val userEmail = principal.name
        boardService.updateBoard(boardId, request, userEmail)
        return ResponseEntity.ok(ApiResponse.onSuccess())
    }

    @DeleteMapping("/{boardId}")
    @PreAuthorize("isAuthenticated()")
    fun deleteBoard(
        @PathVariable boardId: Long,
        principal: Principal
    ): ResponseEntity<ApiResponse<Unit>> {
        val userEmail = principal.name
        boardService.deleteBoard(boardId, userEmail)
        return ResponseEntity.ok(ApiResponse.onSuccess())
    }
}
