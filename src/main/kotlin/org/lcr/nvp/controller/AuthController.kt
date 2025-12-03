package org.lcr.nvp.controller

import jakarta.servlet.http.HttpServletResponse
import org.lcr.nvp.domain.member.application.AuthService
import org.lcr.nvp.domain.member.dto.AccessTokenResponse
import org.lcr.nvp.domain.member.dto.LoginRequest
import org.lcr.nvp.domain.member.dto.LoginResponse
import org.lcr.nvp.domain.member.dto.SignupRequest
import org.lcr.nvp.global.common.ApiResponse
import org.lcr.nvp.global.util.CookieUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Duration

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val cookieUtil: CookieUtil,
    @Value("\${jwt.refresh-expiration-ms}") private val refreshTokenExpirationMs: Long
) {

    @PostMapping("/signup")
    fun signup(@RequestBody signupRequest: SignupRequest): ResponseEntity<ApiResponse<Unit>> {
        authService.signup(signupRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.onSuccess())
    }

    @PostMapping("/login")
    fun login(
        @RequestBody loginRequest: LoginRequest,
        response: HttpServletResponse
    ): ResponseEntity<ApiResponse<LoginResponse>> {
        val (tokenInfo, member) = authService.login(loginRequest)

        // RefreshToken은 HttpOnly 쿠키에 담아서 전달
        val refreshTokenCookie = cookieUtil.createRefreshTokenCookie(
            tokenInfo.refreshToken,
            Duration.ofMillis(refreshTokenExpirationMs)
        )
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())

        // AccessToken은 JSON 본문에 담아서 전달
        val loginResponse = LoginResponse(accessToken = tokenInfo.accessToken, memberId = member?.id)
        return ResponseEntity.ok(ApiResponse.onSuccess(loginResponse))
    }

    @PostMapping("/reissue")
    fun reissue(
        @CookieValue(CookieUtil.REFRESH_TOKEN_COOKIE_NAME) refreshToken: String,
        response: HttpServletResponse
    ): ResponseEntity<ApiResponse<AccessTokenResponse>> {
        val tokenInfo = authService.reissueToken(refreshToken)

        // 새로 발급받은 RefreshToken도 쿠키에 담아서 전달 (Refresh Token Rotation)
        val refreshTokenCookie = cookieUtil.createRefreshTokenCookie(
            tokenInfo.refreshToken,
            Duration.ofMillis(refreshTokenExpirationMs)
        )
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())

        // 새로 발급받은 AccessToken은 JSON 본문에 담아서 전달
        val accessTokenResponse = AccessTokenResponse(accessToken = tokenInfo.accessToken)
        return ResponseEntity.ok(ApiResponse.onSuccess(accessTokenResponse))
    }

    @GetMapping("/hello")
    fun hello(): ResponseEntity<ApiResponse<String>> {
        // 이 API는 SecurityConfig에 의해 인증된 사용자만 접근 가능합니다.
        val message = "Hello, authenticated user!"
        return ResponseEntity.ok(ApiResponse.onSuccess(message))
    }
}
