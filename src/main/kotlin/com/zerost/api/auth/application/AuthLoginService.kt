package com.zerost.api.auth.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.auth.presentation.dto.LoginResponse
import com.zerost.api.auth.domain.RefreshToken
import com.zerost.api.auth.domain.RefreshTokenRepository
import com.zerost.api.user.domain.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class AuthLoginService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authTokenProvider: AuthTokenProvider,
    private val authTokenProperties: AuthTokenProperties,
) {

    @Transactional
    fun login(phoneNumber: String, password: String): LoginResponse {
        val user = userRepository.findByPhoneNumber(phoneNumber)
            .orElseThrow { BusinessException(ErrorCode.INVALID_LOGIN_CREDENTIALS) }

        val passwordHash = user.passwordHash
        if (!user.onboardingCompleted || passwordHash.isNullOrBlank() || !passwordEncoder.matches(password, passwordHash)) {
            throw BusinessException(ErrorCode.INVALID_LOGIN_CREDENTIALS)
        }

        refreshTokenRepository.deleteAllByUserId(requireNotNull(user.id))

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

        return LoginResponse(
            userId = requireNotNull(user.id),
            nickname = requireNotNull(user.nickname),
            phoneNumber = requireNotNull(user.phoneNumber),
            onboardingCompleted = user.onboardingCompleted,
            accessToken = accessToken,
            refreshToken = refreshToken,
            tokenType = "Bearer",
            accessTokenExpiresIn = authTokenProperties.accessTokenExpirationSeconds,
            refreshTokenExpiresIn = authTokenProperties.refreshTokenExpirationSeconds,
        )
    }
}
