package com.zerost.api.auth.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.user.domain.User
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
        )
    }

    private fun Claims.extractUserId(): Long {
        return subject?.toLongOrNull()
            ?: throw BusinessException(ErrorCode.INVALID_ACCESS_TOKEN)
    }
}

data class AccessTokenClaims(
    val userId: Long,
)
