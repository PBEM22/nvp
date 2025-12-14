package org.lcr.nvp.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.lcr.nvp.domain.member.application.AuthService
import org.lcr.nvp.domain.member.dto.AccessTokenResponse
import org.lcr.nvp.domain.member.dto.EmailCheckResponse
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

    @Operation(summary = "이메일 중복 확인", description = "회원가입 시 사용할 이메일이 중복되는지 확인합니다.")
    @GetMapping("/check-email")
    fun checkEmail(@RequestParam email: String): ResponseEntity<ApiResponse<EmailCheckResponse>> {
        val isAvailable = authService.checkEmailAvailability(email)
        return ResponseEntity.ok(ApiResponse.onSuccess(EmailCheckResponse(isAvailable)))
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하고 토큰 및 역할 정보를 발급받습니다.")
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody loginRequest: LoginRequest,
        response: HttpServletResponse
    ): ResponseEntity<ApiResponse<LoginResponse>> {
        val (tokenInfo, member, roles) = authService.login(loginRequest)

        val refreshTokenCookie = cookieUtil.createRefreshTokenCookie(
            tokenInfo.refreshToken,
            Duration.ofMillis(refreshTokenExpirationMs)
        )
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())

        val loginResponse = LoginResponse(
            accessToken = tokenInfo.accessToken,
            memberId = member?.id,
            roles = roles
        )
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

    @Operation(summary = "로그아웃", description = "현재 사용자를 로그아웃 처리하고 토큰을 무효화합니다.")
    @PostMapping("/logout")
    fun logout(
        @RequestHeader("Authorization") authorizationHeader: String,
        response: HttpServletResponse
    ): ResponseEntity<ApiResponse<Unit>> {
        val accessToken = authorizationHeader.substring(7)
        authService.logout(accessToken)

        // 클라이언트의 리프레시 토큰 쿠키를 삭제
        val expiredCookie = cookieUtil.createRefreshTokenCookie("", Duration.ZERO)
        response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie.toString())

        return ResponseEntity.ok(ApiResponse.onSuccess())
    }

    @Operation(summary = "인증 테스트", description = "발급받은 AccessToken이 유효한지 테스트하는 API입니다.")
    @GetMapping("/hello")
    fun hello(): ResponseEntity<ApiResponse<String>> {
        val message = "Hello, authenticated user!"
        return ResponseEntity.ok(ApiResponse.onSuccess(message))
    }
}
