package com.zerost.api.auth.application

import com.zerost.api.auth.domain.RefreshTokenRepository
import com.zerost.api.auth.presentation.dto.RefreshTokenResponse
import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class AuthTokenService(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val authTokenProvider: AuthTokenProvider,
    private val authTokenProperties: AuthTokenProperties,
) {

    @Transactional
    fun refresh(refreshToken: String): RefreshTokenResponse {
        val storedRefreshToken = refreshTokenRepository.findByToken(refreshToken)
            .orElseThrow { BusinessException(ErrorCode.INVALID_REFRESH_TOKEN) }

        if (storedRefreshToken.expiresAt.isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(storedRefreshToken)
            throw BusinessException(ErrorCode.INVALID_REFRESH_TOKEN)
        }

        val user = storedRefreshToken.user
        val newAccessToken = authTokenProvider.createAccessToken(user)
        val newRefreshToken = UUID.randomUUID().toString()
        val newRefreshTokenExpiresAt = LocalDateTime.now().plusSeconds(authTokenProperties.refreshTokenExpirationSeconds)

        storedRefreshToken.rotate(
            token = newRefreshToken,
            expiresAt = newRefreshTokenExpiresAt,
        )

        return RefreshTokenResponse(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken,
            tokenType = "Bearer",
            accessTokenExpiresIn = authTokenProperties.accessTokenExpirationSeconds,
            refreshTokenExpiresIn = authTokenProperties.refreshTokenExpirationSeconds,
        )
    }

    @Transactional
    fun logout(refreshToken: String) {
        val storedRefreshToken = refreshTokenRepository.findByToken(refreshToken)
            .orElseThrow { BusinessException(ErrorCode.INVALID_REFRESH_TOKEN) }
        refreshTokenRepository.delete(storedRefreshToken)
    }
}
