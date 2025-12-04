package org.lcr.nvp.config.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}") private val secretString: String,
    @Value("\${jwt.expiration-ms}") private val accessTokenExpirationMs: Long,
    @Value("\${jwt.refresh-expiration-ms}") private val refreshTokenExpirationMs: Long
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    private val secretKey: SecretKey by lazy {
        val keyBytes = Decoders.BASE64.decode(secretString)
        Keys.hmacShaKeyFor(keyBytes)
    }

    fun generateAccessToken(authentication: Authentication): String {
        val authorities = authentication.authorities.joinToString(",") { it.authority.toString() }
        val now = Date()
        val validity = Date(now.time + accessTokenExpirationMs)

        return Jwts.builder()
            .subject(authentication.name)
            .claim("auth", authorities)
            .issuedAt(now)
            .expiration(validity)
            .signWith(secretKey)
            .compact()
    }

    fun generateRefreshToken(authentication: Authentication): String {
        val now = Date()
        val validity = Date(now.time + refreshTokenExpirationMs)

        return Jwts.builder()
            .subject(authentication.name)
            .issuedAt(now)
            .expiration(validity)
            .signWith(secretKey)
            .compact()
    }

    fun getAuthentication(accessToken: String): Authentication {
        val claims = getClaims(accessToken)
        val authorities: Collection<GrantedAuthority> =
            claims.get("auth", String::class.java)
                .split(",")
                .filter { it.isNotEmpty() }
                .map { SimpleGrantedAuthority(it) }

        val principal = User(claims.subject, "", authorities)
        return UsernamePasswordAuthenticationToken(principal, accessToken, authorities)
    }

    fun getSubject(token: String): String {
        return getClaims(token).subject
    }

    fun validateToken(token: String): Boolean {
        try {
            getClaims(token)
            return true
        } catch (e: SecurityException) {
            log.info("유효하지 않은 JWT 서명입니다.")
        } catch (e: MalformedJwtException) {
            log.info("유효하지 않은 JWT 토큰입니다.")
        } catch (e: ExpiredJwtException) {
            log.info("만료된 JWT 토큰입니다.")
        } catch (e: UnsupportedJwtException) {
            log.info("지원하지 않는 JWT 토큰입니다.")
        } catch (e: IllegalArgumentException) {
            log.info("JWT 클레임이 비어있습니다.")
        }
        return false
    }

    private fun getClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}
