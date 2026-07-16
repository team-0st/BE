package com.zerost.api.user.application

import com.zerost.api.support.createUser
import com.zerost.api.user.domain.UserRepository
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UserRegistrationServiceTest {

    private val userRepository = mock(UserRepository::class.java)
    private val userRegistrationService = UserRegistrationService(userRepository)

    @Test
    fun `처음 등록하는 디바이스 아이디면 유저를 생성한다`() {
        `when`(userRepository.findByDeviceId("device-1")).thenReturn(Optional.empty())
        `when`(userRepository.save(any(com.zerost.api.user.domain.User::class.java))).thenReturn(createUser())

        val response = userRegistrationService.register("device-1")

        assertEquals(1L, response.userId)
        assertFalse(response.onboardingCompleted)
        verify(userRepository).save(any(com.zerost.api.user.domain.User::class.java))
    }

    @Test
    fun `이미 등록된 디바이스 아이디면 기존 유저를 반환한다`() {
        val existingUser = createUser(id = 3L, onboardingCompleted = true)
        `when`(userRepository.findByDeviceId("device-1")).thenReturn(Optional.of(existingUser))

        val response = userRegistrationService.register("device-1")

        assertEquals(3L, response.userId)
        assertTrue(response.onboardingCompleted)
        verify(userRepository, never()).save(any(com.zerost.api.user.domain.User::class.java))
    }
}
