package com.zerost.api.user.application

import com.zerost.api.auth.application.AuthTokenProperties
import com.zerost.api.auth.application.AuthTokenProvider
import com.zerost.api.auth.application.RefreshTokenHasher
import com.zerost.api.auth.domain.RefreshToken
import com.zerost.api.auth.domain.RefreshTokenRepository
import com.zerost.api.ecojam.domain.EcoJamHistory
import com.zerost.api.ecojam.domain.EcoJamHistoryRepository
import com.zerost.api.ecojam.domain.EcoJamHistorySourceType
import com.zerost.api.user.domain.User
import com.zerost.api.user.domain.UserRepository
import com.zerost.api.user.presentation.RegisterUserResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.time.LocalDateTime
import java.util.UUID

@Service
class UserRegistrationService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val authTokenProvider: AuthTokenProvider,
    private val authTokenProperties: AuthTokenProperties,
    private val refreshTokenHasher: RefreshTokenHasher,
    private val ecoJamHistoryRepository: EcoJamHistoryRepository,
) {

    @Transactional
    fun register(): RegisterUserResponse {
        val user = userRepository.save(User())
        ecoJamHistoryRepository.save(
            EcoJamHistory.earn(
                user = user,
                amount = User.SIGNUP_ECO_JAM_BONUS,
                sourceType = EcoJamHistorySourceType.SIGNUP,
                sourceId = 0L,
            ),
        )
        val accessToken = authTokenProvider.createAccessToken(user)
        val refreshToken = UUID.randomUUID().toString()
        val refreshTokenHash = refreshTokenHasher.hash(refreshToken)
        val refreshTokenExpiresAt = LocalDateTime.now().plusSeconds(authTokenProperties.refreshTokenExpirationSeconds)

        refreshTokenRepository.save(
            RefreshToken(
                user = user,
                tokenHash = refreshTokenHash,
                expiresAt = refreshTokenExpiresAt,
            ),
        )

        val userId = requireNotNull(user.id)
        registerAfterCommitLog {
            log.info(
                "user_registered userId={} role={} onboardingCompleted={} ecoJam={}",
                userId,
                user.role,
                user.onboardingCompleted,
                user.ecoJam,
            )
        }

        return RegisterUserResponse(
            userId = userId,
            onboardingCompleted = user.onboardingCompleted,
            accessToken = accessToken,
            refreshToken = refreshToken,
            tokenType = "Bearer",
            accessTokenExpiresIn = authTokenProperties.accessTokenExpirationSeconds,
            refreshTokenExpiresIn = authTokenProperties.refreshTokenExpirationSeconds,
        )
    }

    private fun registerAfterCommitLog(action: () -> Unit) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            action()
            return
        }
        TransactionSynchronizationManager.registerSynchronization(
            object : TransactionSynchronization {
                override fun afterCommit() {
                    action()
                }
            },
        )
    }

    companion object {
        private val log = LoggerFactory.getLogger(UserRegistrationService::class.java)
    }
}
