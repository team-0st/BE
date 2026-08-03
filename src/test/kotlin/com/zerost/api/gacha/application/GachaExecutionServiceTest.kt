package com.zerost.api.gacha.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.ecojam.domain.EcoJamHistoryRepository
import com.zerost.api.gacha.domain.Gacha
import com.zerost.api.gacha.domain.GachaRepository
import com.zerost.api.gacha.domain.GachaRewardPolicy
import com.zerost.api.gacha.domain.GachaRewardPolicyRepository
import com.zerost.api.gacha.domain.GachaRewardType
import com.zerost.api.ingredient.domain.IngredientHistoryRepository
import com.zerost.api.ingredient.domain.IngredientRepository
import com.zerost.api.ingredient.domain.IngredientType
import com.zerost.api.ingredient.domain.UserIngredient
import com.zerost.api.ingredient.domain.UserIngredientRepository
import com.zerost.api.point.application.PointPolicyProperties
import com.zerost.api.point.application.PointAwardService
import com.zerost.api.point.domain.PointHistoryRepository
import com.zerost.api.support.createIngredient
import com.zerost.api.support.createUser
import com.zerost.api.user.domain.UserRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.math.BigDecimal
import java.util.Optional
import kotlin.test.assertEquals

class GachaExecutionServiceTest {

    private val userRepository = mock(UserRepository::class.java)
    private val gachaRewardPolicyRepository = mock(GachaRewardPolicyRepository::class.java)
    private val gachaRepository = mock(GachaRepository::class.java)
    private val ingredientRepository = mock(IngredientRepository::class.java)
    private val userIngredientRepository = mock(UserIngredientRepository::class.java)
    private val ingredientHistoryRepository = mock(IngredientHistoryRepository::class.java)
    private val ecoJamHistoryRepository = mock(EcoJamHistoryRepository::class.java)
    private val pointHistoryRepository = mock(PointHistoryRepository::class.java)
    private val pointAwardService = PointAwardService(userRepository, pointHistoryRepository, PointPolicyProperties())
    private val gachaRandomProvider = mock(GachaRandomProvider::class.java)

    private val gachaExecutionService = GachaExecutionService(
        userRepository = userRepository,
        gachaRewardPolicyRepository = gachaRewardPolicyRepository,
        gachaRepository = gachaRepository,
        ingredientRepository = ingredientRepository,
        userIngredientRepository = userIngredientRepository,
        ingredientHistoryRepository = ingredientHistoryRepository,
        ecoJamHistoryRepository = ecoJamHistoryRepository,
        pointAwardService = pointAwardService,
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

        `when`(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user))
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

        val response = gachaExecutionService.execute(1L)

        assertEquals(10L, response.gachaId)
        assertEquals(100, response.costEcoJam)
        assertEquals(200, response.remainingEcoJam)
        assertEquals("POINT", response.resultType)
        assertEquals(300, response.resultPoint)
        assertEquals(0, response.resultEcoJam)
        assertEquals(200, user.ecoJam)
        assertEquals(300, user.point)
        verify(gachaRepository).save(any(Gacha::class.java))
        verify(ecoJamHistoryRepository).save(any())
        verify(pointHistoryRepository).save(any())
        verify(userIngredientRepository, never()).save(any(UserIngredient::class.java))
    }

    @Test
    fun `재료 보상이면 보유 재료를 증가시키고 사용 이력만 저장한다`() {
        val user = createUser(id = 1L, deviceId = "device-1", ecoJam = 300)
        val ingredient = createIngredient(id = 5L, name = "양배추")
        val ingredientPolicy = GachaRewardPolicy(
            id = 3L,
            name = "일반 재료 1개",
            rewardType = GachaRewardType.INGREDIENT,
            probability = BigDecimal("10.00"),
            ingredient = ingredient,
            ingredientQuantity = 1,
        )

        `when`(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user))
        `when`(gachaRewardPolicyRepository.findAllByActiveTrueOrderByIdAsc()).thenReturn(listOf(ingredientPolicy))
        `when`(gachaRandomProvider.nextInt(1000)).thenReturn(0)
        `when`(userIngredientRepository.findByUserAndIngredient(user, ingredient)).thenReturn(Optional.empty())
        `when`(gachaRepository.save(any(Gacha::class.java))).thenAnswer { invocation ->
            val gacha = invocation.arguments[0] as Gacha
            Gacha(
                id = 11L,
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

        val response = gachaExecutionService.execute(1L)

        assertEquals("INGREDIENT", response.resultType)
        assertEquals(5L, response.resultIngredientId)
        assertEquals(1, response.resultIngredientQuantity)
        assertEquals(200, user.ecoJam)
        verify(userIngredientRepository).save(any(UserIngredient::class.java))
        verify(ingredientHistoryRepository).save(any())
        verify(ecoJamHistoryRepository).save(any())
        verify(pointHistoryRepository, never()).save(any())
    }

    @Test
    fun `랜덤 재료 보상이면 재료 타입 후보 중 하나를 골라 지급한다`() {
        val user = createUser(id = 1L, deviceId = "device-1", ecoJam = 300)
        val firstIngredient = createIngredient(id = 1L, name = "양배추", type = IngredientType.COMMON)
        val secondIngredient = createIngredient(id = 2L, name = "토마토", type = IngredientType.COMMON)
        val ingredientPolicy = GachaRewardPolicy(
            id = 7L,
            name = "랜덤 일반 재료 2개",
            rewardType = GachaRewardType.INGREDIENT,
            probability = BigDecimal("10.00"),
            ingredient = firstIngredient,
            ingredientType = IngredientType.COMMON,
            ingredientQuantity = 2,
        )

        `when`(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user))
        `when`(gachaRewardPolicyRepository.findAllByActiveTrueOrderByIdAsc()).thenReturn(listOf(ingredientPolicy))
        `when`(gachaRandomProvider.nextInt(1000)).thenReturn(0)
        `when`(ingredientRepository.findAllByType(IngredientType.COMMON)).thenReturn(listOf(firstIngredient, secondIngredient))
        `when`(gachaRandomProvider.nextInt(2)).thenReturn(1)
        `when`(userIngredientRepository.findByUserAndIngredient(user, secondIngredient)).thenReturn(Optional.empty())
        `when`(gachaRepository.save(any(Gacha::class.java))).thenAnswer { invocation ->
            val gacha = invocation.arguments[0] as Gacha
            Gacha(
                id = 14L,
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

        val response = gachaExecutionService.execute(1L)

        assertEquals("INGREDIENT", response.resultType)
        assertEquals(2L, response.resultIngredientId)
        assertEquals(2, response.resultIngredientQuantity)
        verify(userIngredientRepository).findByUserAndIngredient(user, secondIngredient)
        verify(userIngredientRepository).save(any(UserIngredient::class.java))
    }

    @Test
    fun `에코잼 보상이면 사용 이력과 적립 이력을 모두 저장한다`() {
        val user = createUser(id = 1L, deviceId = "device-1", ecoJam = 300)
        val ecoJamPolicy = GachaRewardPolicy(
            id = 4L,
            name = "에코잼 30",
            rewardType = GachaRewardType.ECO_JAM,
            probability = BigDecimal("15.00"),
            ecoJamAmount = 30,
        )

        `when`(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user))
        `when`(gachaRewardPolicyRepository.findAllByActiveTrueOrderByIdAsc()).thenReturn(listOf(ecoJamPolicy))
        `when`(gachaRandomProvider.nextInt(1500)).thenReturn(0)
        `when`(gachaRepository.save(any(Gacha::class.java))).thenAnswer { invocation ->
            val gacha = invocation.arguments[0] as Gacha
            Gacha(
                id = 12L,
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

        val response = gachaExecutionService.execute(1L)

        assertEquals("ECO_JAM", response.resultType)
        assertEquals(30, response.resultEcoJam)
        assertEquals(230, user.ecoJam)
        verify(ecoJamHistoryRepository, times(2)).save(any())
        verify(pointHistoryRepository, never()).save(any())
    }

    @Test
    fun `가중치가 0인 정책은 건너뛰고 유효한 정책으로 가챠를 실행한다`() {
        val user = createUser(id = 1L, deviceId = "device-1", ecoJam = 300)
        val skippedPolicy = GachaRewardPolicy(
            id = 4L,
            name = "스킵 대상",
            rewardType = GachaRewardType.FAIL,
            probability = BigDecimal("0.00"),
        )
        val validPolicy = GachaRewardPolicy(
            id = 5L,
            name = "포인트 100",
            rewardType = GachaRewardType.POINT,
            probability = BigDecimal("1.00"),
            pointAmount = 100,
        )

        `when`(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user))
        `when`(gachaRewardPolicyRepository.findAllByActiveTrueOrderByIdAsc()).thenReturn(listOf(skippedPolicy, validPolicy))
        `when`(gachaRandomProvider.nextInt(100)).thenReturn(0)
        `when`(gachaRepository.save(any(Gacha::class.java))).thenAnswer { invocation ->
            val gacha = invocation.arguments[0] as Gacha
            Gacha(
                id = 13L,
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

        val response = gachaExecutionService.execute(1L)

        assertEquals(13L, response.gachaId)
        assertEquals("POINT", response.resultType)
        assertEquals(100, response.resultPoint)
    }

    @Test
    fun `에코잼이 부족하면 가챠를 실행할 수 없다`() {
        val user = createUser(id = 1L, deviceId = "device-1", ecoJam = 50)

        `when`(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user))

        val exception = assertThrows<BusinessException> {
            gachaExecutionService.execute(1L)
        }

        assertEquals(ErrorCode.INSUFFICIENT_ECO_JAM, exception.errorCode)
    }

    @Test
    fun `활성화된 가챠 정책이 없으면 가챠를 실행할 수 없다`() {
        val user = createUser(id = 1L, deviceId = "device-1", ecoJam = 300)

        `when`(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user))
        `when`(gachaRewardPolicyRepository.findAllByActiveTrueOrderByIdAsc()).thenReturn(emptyList())

        val exception = assertThrows<BusinessException> {
            gachaExecutionService.execute(1L)
        }

        assertEquals(ErrorCode.GACHA_REWARD_POLICY_NOT_FOUND, exception.errorCode)
    }

    @Test
    fun `유효한 가중치 정책이 하나도 없으면 가챠를 실행할 수 없다`() {
        val user = createUser(id = 1L, deviceId = "device-1", ecoJam = 300)
        val zeroWeightPolicy = GachaRewardPolicy(
            id = 6L,
            name = "확률 0 정책",
            rewardType = GachaRewardType.FAIL,
            probability = BigDecimal("0.00"),
        )

        `when`(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user))
        `when`(gachaRewardPolicyRepository.findAllByActiveTrueOrderByIdAsc()).thenReturn(listOf(zeroWeightPolicy))

        val exception = assertThrows<BusinessException> {
            gachaExecutionService.execute(1L)
        }

        assertEquals(ErrorCode.INVALID_GACHA_REWARD_POLICY, exception.errorCode)
    }

    @Test
    fun `랜덤 재료 보상인데 후보 재료가 없으면 가챠를 실행할 수 없다`() {
        val user = createUser(id = 1L, deviceId = "device-1", ecoJam = 300)
        val ingredientPolicy = GachaRewardPolicy(
            id = 8L,
            name = "랜덤 히든 재료 1개",
            rewardType = GachaRewardType.INGREDIENT,
            probability = BigDecimal("2.00"),
            ingredientType = IngredientType.HIDDEN,
            ingredientQuantity = 1,
        )

        `when`(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user))
        `when`(gachaRewardPolicyRepository.findAllByActiveTrueOrderByIdAsc()).thenReturn(listOf(ingredientPolicy))
        `when`(gachaRandomProvider.nextInt(200)).thenReturn(0)
        `when`(ingredientRepository.findAllByType(IngredientType.HIDDEN)).thenReturn(emptyList())

        val exception = assertThrows<BusinessException> {
            gachaExecutionService.execute(1L)
        }

        assertEquals(ErrorCode.INGREDIENT_NOT_FOUND, exception.errorCode)
    }
}
