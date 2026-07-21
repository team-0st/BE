package com.zerost.api.user.application

import com.zerost.api.auth.application.AuthTokenProperties
import com.zerost.api.auth.application.AuthTokenProvider
import com.zerost.api.auth.domain.RefreshToken
import com.zerost.api.auth.domain.RefreshTokenRepository
import com.zerost.api.user.domain.User
import com.zerost.api.user.domain.UserRepository
import com.zerost.api.user.presentation.RegisterUserResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class UserRegistrationService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val authTokenProvider: AuthTokenProvider,
    private val authTokenProperties: AuthTokenProperties,
) {

    @Transactional
    fun register(): RegisterUserResponse {
        val user = userRepository.save(User())
        val accessToken = authTokenProvider.createAccessToken(user)
        val refreshToken = UUID.randomUUID().toString()
        val refreshTokenExpiresAt = LocalDateTime.now().plusSeconds(authTokenProperties.refreshTokenExpirationSeconds)

        refreshTokenRepository.save(
            RefreshToken(
                user = user,
                token = refreshToken,
                expiresAt = refreshTokenExpiresAt,
            ),
        )

        return RegisterUserResponse(
            userId = requireNotNull(user.id),
            onboardingCompleted = user.onboardingCompleted,
            accessToken = accessToken,
            refreshToken = refreshToken,
            tokenType = "Bearer",
            accessTokenExpiresIn = authTokenProperties.accessTokenExpirationSeconds,
            refreshTokenExpiresIn = authTokenProperties.refreshTokenExpirationSeconds,
        )
    }
}
