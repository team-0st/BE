package com.zerost.api.gacha.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.gacha.domain.Gacha
import com.zerost.api.gacha.domain.GachaRepository
import com.zerost.api.gacha.domain.GachaRewardPolicy
import com.zerost.api.gacha.domain.GachaRewardType
import com.zerost.api.support.createIngredient
import com.zerost.api.support.createUser
import com.zerost.api.user.domain.UserRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.Optional
import kotlin.test.assertEquals

class GachaQueryServiceTest {

    private val userRepository = mock(UserRepository::class.java)
    private val gachaRepository = mock(GachaRepository::class.java)

    private val gachaQueryService = GachaQueryService(
        userRepository = userRepository,
        gachaRepository = gachaRepository,
    )

    @Test
    fun `가챠 실행 내역을 최신순으로 조회할 수 있다`() {
        val user = createUser(id = 1L, deviceId = "device-1")
        val ingredient = createIngredient(id = 5L, name = "양배추")
        val rewardPolicy = GachaRewardPolicy(
            id = 1L,
            name = "일반 재료 1개",
            rewardType = GachaRewardType.INGREDIENT,
            probability = BigDecimal("10.00"),
            ingredient = ingredient,
            ingredientQuantity = 1,
        )
        val latestGacha = Gacha(
            id = 2L,
            user = user,
            rewardPolicy = rewardPolicy,
            costEcoJam = 100,
            resultType = GachaRewardType.INGREDIENT,
            resultIngredient = ingredient,
            resultIngredientQuantity = 1,
        ).apply {
            createdAt = LocalDateTime.of(2026, 7, 20, 10, 0, 0)
        }
        val olderGacha = Gacha(
            id = 1L,
            user = user,
            rewardPolicy = rewardPolicy,
            costEcoJam = 100,
            resultType = GachaRewardType.POINT,
            resultPoint = 300,
        ).apply {
            createdAt = LocalDateTime.of(2026, 7, 19, 10, 0, 0)
        }

        `when`(userRepository.findByDeviceId("device-1")).thenReturn(Optional.of(user))
        `when`(gachaRepository.findAllByUserIdOrderByCreatedAtDescIdDesc(1L))
            .thenReturn(listOf(latestGacha, olderGacha))

        val response = gachaQueryService.getGachaHistories("device-1")

        assertEquals(2, response.size)
        assertEquals(2L, response[0].gachaId)
        assertEquals("INGREDIENT", response[0].resultType)
        assertEquals(5L, response[0].resultIngredientId)
        assertEquals("양배추", response[0].resultIngredientName)
        assertEquals(1L, response[1].gachaId)
        assertEquals("POINT", response[1].resultType)
        assertEquals(300, response[1].resultPoint)
    }

    @Test
    fun `등록되지 않은 디바이스면 가챠 실행 내역 조회에 실패한다`() {
        `when`(userRepository.findByDeviceId("device-1")).thenReturn(Optional.empty())

        val exception = assertThrows<BusinessException> {
            gachaQueryService.getGachaHistories("device-1")
        }

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.errorCode)
    }
}
