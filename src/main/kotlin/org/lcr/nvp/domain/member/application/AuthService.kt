package org.lcr.nvp.domain.member.application

import org.lcr.nvp.config.jwt.JwtTokenProvider
import org.lcr.nvp.domain.member.domain.Member
import org.lcr.nvp.domain.member.domain.User
import org.lcr.nvp.domain.member.dto.*
import org.lcr.nvp.domain.member.repository.MemberRepository
import org.lcr.nvp.domain.member.repository.RoleRepository
import org.lcr.nvp.domain.member.repository.UserRepository
import org.lcr.nvp.global.exception.domain.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.TimeUnit

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val memberRepository: MemberRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager,
    private val jwtTokenProvider: JwtTokenProvider,
    private val redisTemplate: RedisTemplate<String, String>,
    @Value("\${jwt.refresh-expiration-ms}") private val refreshTokenExpirationMs: Long
) {

    @Transactional
    fun signup(signupRequest: SignupRequest): User {
        if (userRepository.findByEmail(signupRequest.email) != null) {
            throw EmailDuplicationException()
        }

        val defaultRole = roleRepository.findByRoleName("ROLE_USER")
            ?: throw RoleNotFoundException()

        val isMale = when (signupRequest.gender) {
            "남성" -> true
            "여성" -> false
            else -> throw InvalidInputValueException()
        }

        // User 생성
        val user = User(
            email = signupRequest.email,
            password = passwordEncoder.encode(signupRequest.password),
            name = signupRequest.name,
            birthday = signupRequest.birthday,
            isMale = isMale,
            loginType = "LOCAL"
        ).apply {
            roles.add(defaultRole)
        }

        return userRepository.save(user)
    }

    @Transactional
    fun login(loginRequest: LoginRequest): Pair<TokenInfo, Member?> {
        val authentication: Authentication
        try {
            authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(loginRequest.email, loginRequest.password)
            )
        } catch (e: Exception) {
            throw LoginFailedException()
        }

        SecurityContextHolder.getContext().authentication = authentication

        val user = userRepository.findByEmail(loginRequest.email)
            ?: throw UserNotFoundException()

        val member = memberRepository.findByUser(user)

        // 토큰 생성
        val accessToken = jwtTokenProvider.generateAccessToken(authentication)
        val refreshToken = jwtTokenProvider.generateRefreshToken(authentication)

        // Redis에 RefreshToken 저장
        redisTemplate.opsForValue().set(
            authentication.name,
            refreshToken,
            refreshTokenExpirationMs,
            TimeUnit.MILLISECONDS
        )

        val tokenInfo = TokenInfo(accessToken = accessToken, refreshToken = refreshToken)
        return Pair(tokenInfo, member)
    }

    @Transactional
    fun reissueToken(refreshToken: String): TokenInfo {
        // Refresh Token에서 사용자 이메일(subject) 가져오기
        // 이 과정에서 토큰이 유효하지 않으면 SignatureException, ExpiredJwtException 등이 발생
        val userEmail = jwtTokenProvider.getSubject(refreshToken)

        // Redis에 저장된 Refresh Token과 일치하는지 확인
        val storedRefreshToken = redisTemplate.opsForValue().get(userEmail)
        if (storedRefreshToken == null || storedRefreshToken != refreshToken) {
            throw InvalidRefreshTokenException()
        }

        // 새로운 토큰 생성을 위해 사용자 정보 다시 가져오기
        val user = userRepository.findByEmail(userEmail)
            ?: throw UserNotFoundException()
        val authentication = UsernamePasswordAuthenticationToken(
            user.email, null, user.roles.map { org.springframework.security.core.authority.SimpleGrantedAuthority(it.roleName) }
        )

        // 새로운 AccessToken과 RefreshToken 생성 (Refresh Token Rotation)
        val newAccessToken = jwtTokenProvider.generateAccessToken(authentication)
        val newRefreshToken = jwtTokenProvider.generateRefreshToken(authentication)

        // Redis에 새로운 RefreshToken 저장
        redisTemplate.opsForValue().set(
            userEmail,
            newRefreshToken,
            refreshTokenExpirationMs,
            TimeUnit.MILLISECONDS
        )

        return TokenInfo(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken
        )
    }

    @Transactional
    fun logout(accessToken: String) {
        // Access Token에서 사용자 이메일(subject) 가져오기
        // 이 과정에서 토큰이 유효하지 않으면 SignatureException, ExpiredJwtException 등이 발생
        val userEmail = jwtTokenProvider.getSubject(accessToken)

        // Redis에서 해당 사용자의 Refresh Token 삭제
        if (redisTemplate.opsForValue().get(userEmail) != null) {
            redisTemplate.delete(userEmail)
        }

        // Access Token을 블랙리스트에 추가
        val expiration = jwtTokenProvider.getExpiration(accessToken).time - System.currentTimeMillis()
        redisTemplate.opsForValue().set(accessToken, "logout", expiration, TimeUnit.MILLISECONDS)
    }
}
