package org.lcr.nvp.global.exception

import org.lcr.nvp.global.common.ApiResponse
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(this::class.java)

    /**
     * 직접 정의한 BusinessException을 처리합니다.
     */
    @ExceptionHandler(BusinessException::class)
    protected fun handleBusinessException(e: BusinessException): ResponseEntity<ApiResponse<Unit>> {
        log.warn("handleBusinessException: {}", e.message)
        val errorCode = e.errorCode
        val response = ApiResponse.onFailure(errorCode.code, errorCode.message)
        return ResponseEntity.status(errorCode.status).body(response)
    }

    /**
     * 처리하지 못한 모든 예외를 처리합니다.
     */
    @ExceptionHandler(Exception::class)
    protected fun handleException(e: Exception): ResponseEntity<ApiResponse<Unit>> {
        log.error("unhandledException: {}", e.message, e)
        val errorCode = ErrorCode.INTERNAL_SERVER_ERROR
        val response = ApiResponse.onFailure(errorCode.code, errorCode.message)
        return ResponseEntity.status(errorCode.status).body(response)
    }
}
