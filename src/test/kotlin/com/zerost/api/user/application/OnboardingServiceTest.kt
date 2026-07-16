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
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OnboardingServiceTest {

    private val userRepository = mock(UserRepository::class.java)
    private val shopRepository = mock(ShopRepository::class.java)
    private val onboardingService = OnboardingService(userRepository, shopRepository)

    @Test
    fun `온보딩 완료 시 유저 정보와 선택한 상점이 반영된다`() {
        val user = createUser()
        val shop = createShop(id = 2L)
        val command = createOnboardingCommand(shopId = 2L)

        `when`(userRepository.findByDeviceId("device-1")).thenReturn(Optional.of(user))
        `when`(shopRepository.findById(2L)).thenReturn(Optional.of(shop))

        val response = onboardingService.complete(command)

        assertEquals(1L, response.userId)
        assertEquals("펭귄탐험가", response.nickname)
        assertEquals("010-1234-5678", response.phoneNumber)
        assertEquals(2L, response.shopId)
        assertTrue(user.onboardingCompleted)
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
}
