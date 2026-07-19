package com.zerost.api.gacha.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.gacha.domain.Gacha
import com.zerost.api.gacha.domain.GachaRepository
import com.zerost.api.gacha.domain.GachaRewardPolicy
import com.zerost.api.gacha.domain.GachaRewardPolicyRepository
import com.zerost.api.gacha.domain.GachaRewardType
import com.zerost.api.support.createUser
import com.zerost.api.user.domain.UserRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.math.BigDecimal
import java.util.Optional
import kotlin.test.assertEquals

class GachaExecutionServiceTest {

    private val userRepository = mock(UserRepository::class.java)
    private val gachaRewardPolicyRepository = mock(GachaRewardPolicyRepository::class.java)
    private val gachaRepository = mock(GachaRepository::class.java)
    private val gachaRandomProvider = mock(GachaRandomProvider::class.java)

    private val gachaExecutionService = GachaExecutionService(
        userRepository = userRepository,
        gachaRewardPolicyRepository = gachaRewardPolicyRepository,
        gachaRepository = gachaRepository,
        gachaRandomProvider = gachaRandomProvider,
    )

    @Test
    fun `에코잼이 충분하면 가챠를 실행하고 에코잼을 차감할 수 있다`() {
        val user = createUser(id = 1L, deviceId = "device-1", ecoJam = 300)
        val pointPolicy = GachaRewardPolicy(
            id = 2L,
            name = "포인트 300",
            rewardType = GachaRewardType.POINT,
            probability = BigDecimal("5.00"),
            pointAmount = 300,
        )

        `when`(userRepository.findByDeviceIdForUpdate("device-1")).thenReturn(Optional.of(user))
        `when`(gachaRewardPolicyRepository.findAllByActiveTrueOrderByIdAsc()).thenReturn(listOf(pointPolicy))
        `when`(gachaRandomProvider.nextInt(500)).thenReturn(0)
        `when`(gachaRepository.save(any(Gacha::class.java))).thenAnswer { invocation ->
            val gacha = invocation.arguments[0] as Gacha
            Gacha(
                id = 10L,
                user = gacha.user,
                rewardPolicy = gacha.rewardPolicy,
                costEcoJam = gacha.costEcoJam,
                resultType = gacha.resultType,
                resultPoint = gacha.resultPoint,
                resultEcoJam = gacha.resultEcoJam,
                resultIngredient = gacha.resultIngredient,
                resultIngredientQuantity = gacha.resultIngredientQuantity,
            )
        }

        val response = gachaExecutionService.execute("device-1")

        assertEquals(10L, response.gachaId)
        assertEquals(100, response.costEcoJam)
        assertEquals(200, response.remainingEcoJam)
        assertEquals("POINT", response.resultType)
        assertEquals(300, response.resultPoint)
        assertEquals(0, response.resultEcoJam)
        assertEquals(200, user.ecoJam)
        verify(gachaRepository).save(any(Gacha::class.java))
    }

    @Test
    fun `에코잼이 부족하면 가챠를 실행할 수 없다`() {
        val user = createUser(id = 1L, deviceId = "device-1", ecoJam = 50)

        `when`(userRepository.findByDeviceIdForUpdate("device-1")).thenReturn(Optional.of(user))

        val exception = assertThrows<BusinessException> {
            gachaExecutionService.execute("device-1")
        }

        assertEquals(ErrorCode.INSUFFICIENT_ECO_JAM, exception.errorCode)
    }

    @Test
    fun `활성화된 가챠 정책이 없으면 가챠를 실행할 수 없다`() {
        val user = createUser(id = 1L, deviceId = "device-1", ecoJam = 300)

        `when`(userRepository.findByDeviceIdForUpdate("device-1")).thenReturn(Optional.of(user))
        `when`(gachaRewardPolicyRepository.findAllByActiveTrueOrderByIdAsc()).thenReturn(emptyList())

        val exception = assertThrows<BusinessException> {
            gachaExecutionService.execute("device-1")
        }

        assertEquals(ErrorCode.GACHA_REWARD_POLICY_NOT_FOUND, exception.errorCode)
    }
}
