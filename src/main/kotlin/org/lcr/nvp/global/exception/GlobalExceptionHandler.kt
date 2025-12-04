package org.lcr.nvp.global.exception

import io.jsonwebtoken.security.SignatureException
import org.lcr.nvp.global.common.ApiResponse
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AuthorizationServiceException
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(this::class.java)

    /**
     * @Valid 어노테이션을 사용한 유효성 검사에 실패했을 때 발생하는 예외를 처리합니다.
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    protected fun handleMethodArgumentNotValid(e: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Unit>> {
        log.warn("handleMethodArgumentNotValidException: {}", e.message)
        val errorCode = ErrorCode.INVALID_INPUT_VALUE
        val response = ApiResponse.onFailure(errorCode.code, e.bindingResult.fieldError?.defaultMessage ?: errorCode.message)
        return ResponseEntity.status(errorCode.status).body(response)
    }

    /**
     * JWT 서명 관련 예외를 처리합니다.
     */
    @ExceptionHandler(SignatureException::class)
    protected fun handleSignatureException(e: SignatureException): ResponseEntity<ApiResponse<Unit>> {
        log.warn("handleSignatureException: {}", e.message)
        val errorCode = ErrorCode.INVALID_TOKEN
        val response = ApiResponse.onFailure(errorCode.code, "JWT 서명이 유효하지 않습니다.")
        return ResponseEntity.status(errorCode.status).body(response)
    }

    /**
     * 권한 부족 예외를 처리합니다. (@PreAuthorize 등에서 발생)
     */
    @ExceptionHandler(AuthorizationDeniedException::class)
    protected fun handleAuthorizationDeniedException(e: AuthorizationDeniedException): ResponseEntity<ApiResponse<Unit>> {
        log.warn("handleAuthorizationDeniedException: {}", e.message)
        val errorCode = ErrorCode.FORBIDDEN
        val response = ApiResponse.onFailure(errorCode.code, errorCode.message)
        return ResponseEntity.status(errorCode.status).body(response)
    }

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
