package org.lcr.nvp.config.security.oauth

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.lcr.nvp.config.jwt.JwtTokenProvider
import org.lcr.nvp.domain.member.repository.UserRepository
import org.lcr.nvp.global.exception.domain.UserNotFoundException
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder

@Component
class OAuth2AuthenticationSuccessHandler(
    private val jwtTokenProvider: JwtTokenProvider,
    private val userRepository: UserRepository,
    @Value("\${oauth.redirect-uri}") private val redirectUri: String
) : SimpleUrlAuthenticationSuccessHandler() {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val oAuth2User = authentication.principal as OAuth2User
        val email = (oAuth2User.attributes["kakao_account"] as Map<*, *>)["email"] as String
        
        // CustomOAuth2UserService에서 이미 사용자 생성을 보장하므로, 여기서는 찾기만 하면 됨
        val user = userRepository.findByEmail(email) ?: throw UserNotFoundException()
        
        // 우리 서비스의 자체 인증 객체 생성 (Principal을 email로 설정)
        val appAuthentication = UsernamePasswordAuthenticationToken(
            user.email,
            null,
            user.roles.map { SimpleGrantedAuthority(it.roleName) }
        )

        // 우리 서비스의 자체 JWT 생성 (새로 만든 인증 객체 사용)
        val appToken = jwtTokenProvider.generateAccessToken(appAuthentication)

        // 프론트엔드로 리다이렉트할 URL 생성 (토큰을 쿼리 파라미터로 포함)
        val targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
            .queryParam("token", appToken)
            .build().toUriString()

        // 리다이렉트
        clearAuthenticationAttributes(request)
        redirectStrategy.sendRedirect(request, response, targetUrl)
    }
}
