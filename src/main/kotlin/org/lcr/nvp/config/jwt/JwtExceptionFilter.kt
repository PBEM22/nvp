package org.lcr.nvp.config.jwt

import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.SignatureException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.lcr.nvp.global.common.ApiResponse
import org.lcr.nvp.global.exception.BusinessException
import org.lcr.nvp.global.exception.ErrorCode
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtExceptionFilter : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(this::class.java)
    private val objectMapper = ObjectMapper()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            filterChain.doFilter(request, response)
        } catch (e: BusinessException) {
            log.warn("BusinessException in filter chain: ${e.errorCode.message}")
            setErrorResponse(response, e.errorCode)
        } catch (e: SignatureException) {
            log.warn("JWT SignatureException: ${e.message}")
            setErrorResponse(response, ErrorCode.INVALID_TOKEN, "유효하지 않은 토큰 서명입니다.")
        } catch (e: MalformedJwtException) {
            log.warn("JWT MalformedJwtException: ${e.message}")
            setErrorResponse(response, ErrorCode.INVALID_TOKEN, "손상된 토큰입니다.")
        } catch (e: ExpiredJwtException) {
            log.warn("JWT ExpiredJwtException: ${e.message}")
            setErrorResponse(response, ErrorCode.EXPIRED_TOKEN)
        } catch (e: UnsupportedJwtException) {
            log.warn("JWT UnsupportedJwtException: ${e.message}")
            setErrorResponse(response, ErrorCode.INVALID_TOKEN, "지원하지 않는 토큰 형식입니다.")
        } catch (e: IllegalArgumentException) {
            log.warn("JWT IllegalArgumentException: ${e.message}")
            setErrorResponse(response, ErrorCode.INVALID_TOKEN, "JWT 클레임이 비어있습니다.")
        } catch (e: Exception) {
            log.error("Unhandled Exception in filter chain", e)
            setErrorResponse(response, ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }

    private fun setErrorResponse(response: HttpServletResponse, errorCode: ErrorCode, message: String = errorCode.message) {
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"
        response.status = errorCode.status.value()

        val apiResponse = ApiResponse.onFailure(errorCode.code, message)
        val jsonResponse = objectMapper.writeValueAsString(apiResponse)
        response.writer.write(jsonResponse)
    }
}
