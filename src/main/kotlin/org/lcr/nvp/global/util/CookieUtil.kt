package org.lcr.nvp.global.util

import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class CookieUtil {

    fun createRefreshTokenCookie(token: String, duration: Duration): ResponseCookie {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, token)
            .httpOnly(true) // JavaScript에서 접근 불가
            .secure(true)   // HTTPS 통신 시에만 전송
            .path("/")      // 쿠키가 전송될 URL 경로
            .maxAge(duration) // 쿠키 만료 시간
            .sameSite("Lax") // CSRF 공격 방지를 위한 설정
            .build()
    }

    companion object {
        const val REFRESH_TOKEN_COOKIE_NAME = "refresh_token"
    }
}
