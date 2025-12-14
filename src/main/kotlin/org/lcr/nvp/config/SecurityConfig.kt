package org.lcr.nvp.config

import org.lcr.nvp.config.jwt.JwtAuthenticationFilter
import org.lcr.nvp.config.jwt.JwtExceptionFilter
import org.lcr.nvp.config.security.oauth.CustomOAuth2UserService
import org.lcr.nvp.config.security.oauth.OAuth2AuthenticationSuccessHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // 메소드 수준의 보안 설정을 활성화
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val jwtExceptionFilter: JwtExceptionFilter,
    private val customOAuth2UserService: CustomOAuth2UserService,
    private val oAuth2AuthenticationSuccessHandler: OAuth2AuthenticationSuccessHandler
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun authenticationManager(authenticationConfiguration: AuthenticationConfiguration): AuthenticationManager {
        return authenticationConfiguration.authenticationManager
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() } // CSRF 보호 비활성화 (stateless 이므로)
            .httpBasic { it.disable() } // HTTP Basic 인증 비활성화
            .formLogin { it.disable() } // Form Login 비활성화
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) } // 세션 관리 STATELESS 설정
            .authorizeHttpRequests { authorize ->
                authorize
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // Preflight 요청은 항상 허용
                    .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "MANAGER")
                    .requestMatchers(
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/error",
                        "/api/auth/**"
                    ).permitAll()
                    .requestMatchers(
                        HttpMethod.GET,
                        "/api/v1/members/{memberId:[0-9]+}",
                        "/api/v1/members/{memberId:[0-9]+}/score-record",
                        "/api/v1/members/{memberId:[0-9]+}/matches",
                        "/api/v1/members/{memberId:[0-9]+}/tournaments",
                        "/api/v1/matches/{matchId:[0-9]+}/details",
                        "/api/v1/matches/{matchId:[0-9]+}/records",
                        "/api/v1/tournaments",
                        "/api/v1/tournaments/{tournamentId:[0-9]+}",
                        "/api/v1/tournaments/{tournamentId:[0-9]+}/matches",
                        "/api/v1/opponent-schools",
                        "/api/v1/opponent-schools/{schoolId:[0-9]+}",
                        "/api/boards",
                        "/api/boards/{boardId:[0-9]+}",
                        "/api/boards/{boardId:[0-9]+}/comments"
                    ).permitAll()
                    .anyRequest().authenticated()
            }
            .oauth2Login { oauth2 ->
                oauth2.userInfoEndpoint { userInfo ->
                    userInfo.userService(customOAuth2UserService) // 사용자 정보 처리 서비스 설정
                }
                oauth2.successHandler(oAuth2AuthenticationSuccessHandler) // 로그인 성공 후 처리 핸들러 설정
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterBefore(jwtExceptionFilter, JwtAuthenticationFilter::class.java) // 예외 처리 필터를 인증 필터 앞에 추가

        return http.build()
    }
}
