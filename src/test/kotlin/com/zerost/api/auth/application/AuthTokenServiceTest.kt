package com.zerost.api.auth.application

import com.zerost.api.auth.domain.RefreshToken
import com.zerost.api.auth.domain.RefreshTokenRepository
import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.support.createUser
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class AuthTokenServiceTest {

    private val refreshTokenRepository = mock(RefreshTokenRepository::class.java)
    private val authTokenProperties = AuthTokenProperties(
        secret = "test-secret-key-test-secret-key-1234",
        accessTokenExpirationSeconds = 3600,
        refreshTokenExpirationSeconds = 1209600,
    )
    private val authTokenProvider = AuthTokenProvider(authTokenProperties)
    private val authTokenService = AuthTokenService(
        refreshTokenRepository = refreshTokenRepository,
        authTokenProvider = authTokenProvider,
        authTokenProperties = authTokenProperties,
    )

    @Test
    fun `유효한 refresh token이면 토큰을 재발급한다`() {
        val user = createUser(
            onboardingCompleted = true,
            nickname = "펭귄탐험가",
            phoneNumber = "010-1234-5678",
        )
        val storedRefreshToken = RefreshToken(
            id = 1L,
            user = user,
            token = "refresh-token",
            expiresAt = LocalDateTime.now().plusDays(7),
        )

        `when`(refreshTokenRepository.findByToken("refresh-token")).thenReturn(Optional.of(storedRefreshToken))

        val response = authTokenService.refresh("refresh-token")

        assertTrue(response.accessToken.isNotBlank())
        assertNotEquals("refresh-token", response.refreshToken)
        assertEquals("Bearer", response.tokenType)
        assertEquals(response.refreshToken, storedRefreshToken.token)
    }

    @Test
    fun `만료된 refresh token이면 재발급에 실패한다`() {
        val user = createUser(
            onboardingCompleted = true,
            nickname = "펭귄탐험가",
            phoneNumber = "010-1234-5678",
        )
        val storedRefreshToken = RefreshToken(
            id = 1L,
            user = user,
            token = "refresh-token",
            expiresAt = LocalDateTime.now().minusMinutes(1),
        )
        `when`(refreshTokenRepository.findByToken("refresh-token")).thenReturn(Optional.of(storedRefreshToken))

        val exception = assertFailsWith<BusinessException> {
            authTokenService.refresh("refresh-token")
        }

        assertEquals(ErrorCode.INVALID_REFRESH_TOKEN, exception.errorCode)
        verify(refreshTokenRepository).delete(storedRefreshToken)
    }

    @Test
    fun `로그아웃 시 refresh token을 삭제한다`() {
        val user = createUser(
            onboardingCompleted = true,
            nickname = "펭귄탐험가",
            phoneNumber = "010-1234-5678",
        )
        val storedRefreshToken = RefreshToken(
            id = 1L,
            user = user,
            token = "refresh-token",
            expiresAt = LocalDateTime.now().plusDays(7),
        )
        `when`(refreshTokenRepository.findByToken("refresh-token")).thenReturn(Optional.of(storedRefreshToken))

        authTokenService.logout("refresh-token")

        verify(refreshTokenRepository).delete(storedRefreshToken)
    }
}
