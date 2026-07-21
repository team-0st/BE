package com.zerost.api.user.domain

import com.zerost.api.support.createShop
import com.zerost.api.support.createUser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UserTest {

    @Test
    fun `온보딩을 완료하면 프로필 정보가 저장되고 완료 상태가 된다`() {
        val user = createUser()
        val shop = createShop(id = 10L)

        user.completeOnboarding(
            nickname = "펭귄탐험가",
            phoneNumber = "010-1234-5678",
            passwordHash = "encoded-password",
            shop = shop,
        )

        assertEquals("펭귄탐험가", user.nickname)
        assertEquals("010-1234-5678", user.phoneNumber)
        assertEquals("encoded-password", user.passwordHash)
        assertEquals(shop, user.shop)
        assertTrue(user.onboardingCompleted)
    }

    @Test
    fun `신규 유저의 온보딩 완료 상태는 기본값이 false다`() {
        val user = createUser()

        assertFalse(user.onboardingCompleted)
    }
}
