package com.zerost.api.auth.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.common.config.PublicAssetsProperties
import com.zerost.api.auth.domain.RefreshToken
import com.zerost.api.auth.domain.RefreshTokenRepository
import com.zerost.api.profile.application.ProfileCharacterImageUrlResolver
import com.zerost.api.support.createUser
import com.zerost.api.user.domain.ProfileCharacterCode
import com.zerost.api.user.domain.UserRepository
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.time.LocalDateTime
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class AuthLoginServiceTest {

    private val userRepository = mock(UserRepository::class.java)
    private val refreshTokenRepository = mock(RefreshTokenRepository::class.java)
    private val passwordEncoder = BCryptPasswordEncoder()
    private val authTokenProperties = AuthTokenProperties(
        secret = "test-secret-key-test-secret-key-1234",
        accessTokenExpirationSeconds = 3600,
        refreshTokenExpirationSeconds = 1209600,
    )
    private val authTokenProvider = AuthTokenProvider(authTokenProperties)
    private val refreshTokenHasher = RefreshTokenHasher()
    private val profileCharacterImageUrlResolver = ProfileCharacterImageUrlResolver(
        PublicAssetsProperties(
            baseUrl = "https://assets.zero-st.com",
        ),
    )
    private val authLoginService = AuthLoginService(
        userRepository = userRepository,
        refreshTokenRepository = refreshTokenRepository,
        passwordEncoder = passwordEncoder,
        authTokenProvider = authTokenProvider,
        authTokenProperties = authTokenProperties,
        refreshTokenHasher = refreshTokenHasher,
        profileCharacterImageUrlResolver = profileCharacterImageUrlResolver,
    )

    @Test
    fun `휴대전화 번호와 비밀번호가 맞으면 로그인한다`() {
        val user = createUser(
            onboardingCompleted = true,
            nickname = "펭귄탐험가",
            phoneNumber = "010-1234-5678",
            profileCharacterCode = ProfileCharacterCode.TOMATO,
            passwordHash = passwordEncoder.encode("zerost1234"),
        )
        `when`(userRepository.findByPhoneNumber("010-1234-5678")).thenReturn(Optional.of(user))
        `when`(refreshTokenRepository.save(any(RefreshToken::class.java))).thenAnswer { it.arguments[0] as RefreshToken }

        val response = authLoginService.login("010-1234-5678", "zerost1234")

        assertEquals(1L, response.userId)
        assertEquals("펭귄탐험가", response.nickname)
        assertEquals("TOMATO", response.profileCharacterCode)
        assertEquals("https://assets.zero-st.com/profile-characters/tomato.png", response.profileCharacterImageUrl)
        assertEquals("Bearer", response.tokenType)
        assertTrue(response.accessToken.isNotBlank())
        assertTrue(response.refreshToken.isNotBlank())
        verify(refreshTokenRepository).deleteAllByUserId(1L)
    }

    @Test
    fun `비밀번호가 틀리면 로그인에 실패한다`() {
        val user = createUser(
            onboardingCompleted = true,
            nickname = "펭귄탐험가",
            phoneNumber = "010-1234-5678",
            passwordHash = passwordEncoder.encode("zerost1234"),
        )
        `when`(userRepository.findByPhoneNumber("010-1234-5678")).thenReturn(Optional.of(user))

        val exception = assertFailsWith<BusinessException> {
            authLoginService.login("010-1234-5678", "wrong-pass")
        }

        assertEquals(ErrorCode.INVALID_LOGIN_CREDENTIALS, exception.errorCode)
    }

    @Test
    fun `온보딩 미완료 유저는 로그인할 수 없다`() {
        val user = createUser(
            onboardingCompleted = false,
            phoneNumber = "010-1234-5678",
            passwordHash = passwordEncoder.encode("zerost1234"),
        )
        `when`(userRepository.findByPhoneNumber("010-1234-5678")).thenReturn(Optional.of(user))

        val exception = assertFailsWith<BusinessException> {
            authLoginService.login("010-1234-5678", "zerost1234")
        }

        assertEquals(ErrorCode.INVALID_LOGIN_CREDENTIALS, exception.errorCode)
    }
}
