package com.zerost.api.user.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.shop.domain.ShopRepository
import com.zerost.api.support.createOnboardingCommand
import com.zerost.api.support.createShop
import com.zerost.api.support.createUser
import com.zerost.api.user.domain.UserRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OnboardingServiceTest {

    private val userRepository = mock(UserRepository::class.java)
    private val shopRepository = mock(ShopRepository::class.java)
    private val passwordEncoder = BCryptPasswordEncoder()
    private val onboardingService = OnboardingService(userRepository, shopRepository, passwordEncoder)

    @Test
    fun `온보딩 완료 시 유저 정보와 선택한 상점이 반영된다`() {
        val user = createUser()
        val shop = createShop(id = 2L)
        val command = createOnboardingCommand(shopId = 2L)

        `when`(userRepository.findByDeviceId("device-1")).thenReturn(Optional.of(user))
        `when`(shopRepository.findById(2L)).thenReturn(Optional.of(shop))
        `when`(userRepository.findByNickname("펭귄탐험가")).thenReturn(Optional.empty())
        `when`(userRepository.findByPhoneNumber("010-1234-5678")).thenReturn(Optional.empty())

        val response = onboardingService.complete(command)

        assertEquals(1L, response.userId)
        assertEquals("펭귄탐험가", response.nickname)
        assertEquals("010-1234-5678", response.phoneNumber)
        assertEquals(2L, response.shopId)
        assertTrue(user.onboardingCompleted)
        assertTrue(passwordEncoder.matches("zerost1234", requireNotNull(user.passwordHash)))
    }

    @Test
    fun `등록되지 않은 유저면 온보딩 완료에 실패한다`() {
        val command = createOnboardingCommand(deviceId = "missing-device", shopId = 2L)
        `when`(userRepository.findByDeviceId("missing-device")).thenReturn(Optional.empty())

        val exception = assertThrows<BusinessException> {
            onboardingService.complete(command)
        }

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.errorCode)
    }

    @Test
    fun `존재하지 않는 상점이면 온보딩 완료에 실패한다`() {
        val user = createUser()
        val command = createOnboardingCommand(shopId = 999L)
        `when`(userRepository.findByDeviceId("device-1")).thenReturn(Optional.of(user))
        `when`(shopRepository.findById(999L)).thenReturn(Optional.empty())

        val exception = assertThrows<BusinessException> {
            onboardingService.complete(command)
        }

        assertEquals(ErrorCode.SHOP_NOT_FOUND, exception.errorCode)
    }

    @Test
    fun `이미 사용 중인 닉네임이면 온보딩 완료에 실패한다`() {
        val user = createUser(id = 1L)
        val duplicatedUser = createUser(id = 2L, nickname = "펭귄탐험가")
        val shop = createShop(id = 2L)
        val command = createOnboardingCommand(shopId = 2L)
        `when`(userRepository.findByDeviceId("device-1")).thenReturn(Optional.of(user))
        `when`(shopRepository.findById(2L)).thenReturn(Optional.of(shop))
        `when`(userRepository.findByNickname("펭귄탐험가")).thenReturn(Optional.of(duplicatedUser))

        val exception = assertThrows<BusinessException> {
            onboardingService.complete(command)
        }

        assertEquals(ErrorCode.DUPLICATE_NICKNAME, exception.errorCode)
    }

    @Test
    fun `이미 사용 중인 휴대전화 번호면 온보딩 완료에 실패한다`() {
        val user = createUser(id = 1L)
        val duplicatedUser = createUser(id = 2L, phoneNumber = "010-1234-5678")
        val shop = createShop(id = 2L)
        val command = createOnboardingCommand(shopId = 2L)
        `when`(userRepository.findByDeviceId("device-1")).thenReturn(Optional.of(user))
        `when`(shopRepository.findById(2L)).thenReturn(Optional.of(shop))
        `when`(userRepository.findByNickname("펭귄탐험가")).thenReturn(Optional.empty())
        `when`(userRepository.findByPhoneNumber("010-1234-5678")).thenReturn(Optional.of(duplicatedUser))

        val exception = assertThrows<BusinessException> {
            onboardingService.complete(command)
        }

        assertEquals(ErrorCode.DUPLICATE_PHONE_NUMBER, exception.errorCode)
    }
}
