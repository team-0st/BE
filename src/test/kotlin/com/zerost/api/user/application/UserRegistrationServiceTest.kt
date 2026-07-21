package com.zerost.api.user.application

import com.zerost.api.auth.application.AuthTokenProperties
import com.zerost.api.auth.application.AuthTokenProvider
import com.zerost.api.auth.domain.RefreshTokenRepository
import com.zerost.api.support.createUser
import com.zerost.api.user.domain.UserRepository
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class UserRegistrationServiceTest {

    private val userRepository = mock(UserRepository::class.java)
    private val refreshTokenRepository = mock(RefreshTokenRepository::class.java)
    private val authTokenProvider = mock(AuthTokenProvider::class.java)
    private val authTokenProperties = AuthTokenProperties(
        secret = "test-secret-key-test-secret-key-1234",
        accessTokenExpirationSeconds = 3600,
        refreshTokenExpirationSeconds = 1209600,
    )
    private val userRegistrationService = UserRegistrationService(
        userRepository = userRepository,
        refreshTokenRepository = refreshTokenRepository,
        authTokenProvider = authTokenProvider,
        authTokenProperties = authTokenProperties,
    )

    @Test
    fun `임시 유저를 생성하고 토큰을 발급한다`() {
        val savedUser = createUser()
        `when`(userRepository.save(any(com.zerost.api.user.domain.User::class.java))).thenReturn(savedUser)
        `when`(authTokenProvider.createAccessToken(savedUser)).thenReturn("access-token")

        val response = userRegistrationService.register()

        assertEquals(1L, response.userId)
        assertFalse(response.onboardingCompleted)
        assertEquals("access-token", response.accessToken)
        assertEquals("Bearer", response.tokenType)
        verify(userRepository).save(any(com.zerost.api.user.domain.User::class.java))
        verify(refreshTokenRepository).save(any())
    }
}
