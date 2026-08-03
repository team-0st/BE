package com.zerost.api.auth.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.user.domain.User
import com.zerost.api.user.domain.UserRole
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.Date
import javax.crypto.SecretKey

@Component
class AuthTokenProvider(
    private val authTokenProperties: AuthTokenProperties,
) {

    private val signingKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(authTokenProperties.secret.toByteArray(StandardCharsets.UTF_8))
    }

    fun createAccessToken(user: User): String {
        val now = Instant.now()
        val expiresAt = now.plusSeconds(authTokenProperties.accessTokenExpirationSeconds)

        return Jwts.builder()
            .subject(requireNotNull(user.id).toString())
            .claim(ROLE_CLAIM_NAME, user.role.name)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiresAt))
            .signWith(signingKey)
            .compact()
    }

    fun parseAccessToken(token: String): AccessTokenClaims {
        val claims = runCatching {
            Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .payload
        }.getOrElse {
            throw BusinessException(ErrorCode.INVALID_ACCESS_TOKEN)
        }

        return AccessTokenClaims(
            userId = claims.extractUserId(),
            role = claims.extractUserRole(),
        )
    }

    private fun Claims.extractUserId(): Long {
        return subject?.toLongOrNull()
            ?: throw BusinessException(ErrorCode.INVALID_ACCESS_TOKEN)
    }

    private fun Claims.extractUserRole(): UserRole {
        val rawRole = get(ROLE_CLAIM_NAME, String::class.java)
        return rawRole?.let {
            runCatching { UserRole.valueOf(it) }.getOrNull()
        } ?: throw BusinessException(ErrorCode.INVALID_ACCESS_TOKEN)
    }

    companion object {
        private const val ROLE_CLAIM_NAME = "role"
    }
}

data class AccessTokenClaims(
    val userId: Long,
    val role: UserRole,
)
