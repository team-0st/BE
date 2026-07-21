package com.zerost.api.auth.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.support.createUser
import com.zerost.api.user.domain.UserRepository
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AuthLoginServiceTest {

    private val userRepository = mock(UserRepository::class.java)
    private val passwordEncoder = BCryptPasswordEncoder()
    private val authLoginService = AuthLoginService(userRepository, passwordEncoder)

    @Test
    fun `휴대전화 번호와 비밀번호가 맞으면 로그인한다`() {
        val user = createUser(
            onboardingCompleted = true,
            nickname = "펭귄탐험가",
            phoneNumber = "010-1234-5678",
            passwordHash = passwordEncoder.encode("zerost1234"),
        )
        `when`(userRepository.findByPhoneNumber("010-1234-5678")).thenReturn(Optional.of(user))

        val response = authLoginService.login("010-1234-5678", "zerost1234")

        assertEquals(1L, response.userId)
        assertEquals("device-1", response.deviceId)
        assertEquals("펭귄탐험가", response.nickname)
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
