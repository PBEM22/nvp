package org.lcr.nvp.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
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

@Tag(name = "인증 API", description = "사용자 회원가입, 로그인, 토큰 재발급 관련 API")
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val cookieUtil: CookieUtil,
    @Value("\${jwt.refresh-expiration-ms}") private val refreshTokenExpirationMs: Long
) {

    @Operation(summary = "회원가입", description = "새로운 사용자를 시스템에 등록합니다.")
    @PostMapping("/signup")
    fun signup(@Valid @RequestBody signupRequest: SignupRequest): ResponseEntity<ApiResponse<Unit>> {
        authService.signup(signupRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.onSuccess())
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하고 토큰을 발급받습니다.")
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody loginRequest: LoginRequest,
        response: HttpServletResponse
    ): ResponseEntity<ApiResponse<LoginResponse>> {
        val (tokenInfo, member) = authService.login(loginRequest)

        val refreshTokenCookie = cookieUtil.createRefreshTokenCookie(
            tokenInfo.refreshToken,
            Duration.ofMillis(refreshTokenExpirationMs)
        )
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())

        val loginResponse = LoginResponse(accessToken = tokenInfo.accessToken, memberId = member?.id)
        return ResponseEntity.ok(ApiResponse.onSuccess(loginResponse))
    }

    @Operation(
        summary = "토큰 재발급",
        description = "HttpOnly 쿠키에 담긴 RefreshToken을 사용하여 새로운 AccessToken과 RefreshToken을 발급받습니다."
    )
    @PostMapping("/reissue")
    fun reissue(
        @Parameter(hidden = true) @CookieValue(CookieUtil.REFRESH_TOKEN_COOKIE_NAME) refreshToken: String,
        response: HttpServletResponse
    ): ResponseEntity<ApiResponse<AccessTokenResponse>> {
        val tokenInfo = authService.reissueToken(refreshToken)

        val refreshTokenCookie = cookieUtil.createRefreshTokenCookie(
            tokenInfo.refreshToken,
            Duration.ofMillis(refreshTokenExpirationMs)
        )
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())

        val accessTokenResponse = AccessTokenResponse(accessToken = tokenInfo.accessToken)
        return ResponseEntity.ok(ApiResponse.onSuccess(accessTokenResponse))
    }

    @Operation(summary = "인증 테스트", description = "발급받은 AccessToken이 유효한지 테스트하는 API입니다.")
    @GetMapping("/hello")
    fun hello(): ResponseEntity<ApiResponse<String>> {
        val message = "Hello, authenticated user!"
        return ResponseEntity.ok(ApiResponse.onSuccess(message))
    }
}
