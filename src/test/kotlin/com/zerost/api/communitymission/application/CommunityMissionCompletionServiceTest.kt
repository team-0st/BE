package com.zerost.api.communitymission.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.communitymission.domain.CommunityMissionCompletion
import com.zerost.api.communitymission.domain.CommunityMissionCompletionRepository
import com.zerost.api.communitymission.domain.CommunityMissionRepository
import com.zerost.api.communitymission.domain.CommunityMissionDifficulty
import com.zerost.api.communitymission.domain.CommunityMissionReward
import com.zerost.api.communitymission.domain.CommunityMissionRewardRepository
import com.zerost.api.communitymission.domain.CommunityMissionRewardType
import com.zerost.api.ecojam.domain.EcoJamHistoryRepository
import com.zerost.api.ingredient.domain.IngredientHistoryRepository
import com.zerost.api.ingredient.domain.IngredientRepository
import com.zerost.api.ingredient.domain.IngredientType
import com.zerost.api.ingredient.domain.UserIngredientRepository
import com.zerost.api.support.createCommunityMission
import com.zerost.api.support.createCommunityMissionReward
import com.zerost.api.support.createIngredient
import com.zerost.api.support.createUser
import com.zerost.api.support.createUserIngredient
import com.zerost.api.user.domain.UserRepository
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CommunityMissionCompletionServiceTest {

    private val userRepository = mock(UserRepository::class.java)
    private val communityMissionRepository = mock(CommunityMissionRepository::class.java)
    private val communityMissionCompletionRepository = mock(CommunityMissionCompletionRepository::class.java)
    private val communityMissionRewardRepository = mock(CommunityMissionRewardRepository::class.java)
    private val ingredientRepository = mock(IngredientRepository::class.java)
    private val userIngredientRepository = mock(UserIngredientRepository::class.java)
    private val ecoJamHistoryRepository = mock(EcoJamHistoryRepository::class.java)
    private val ingredientHistoryRepository = mock(IngredientHistoryRepository::class.java)
    private val communityMissionRewardRandomProvider = mock(CommunityMissionRewardRandomProvider::class.java)
    private val communityMissionCompletionService = CommunityMissionCompletionService(
        userRepository = userRepository,
        communityMissionRepository = communityMissionRepository,
        communityMissionCompletionRepository = communityMissionCompletionRepository,
        communityMissionRewardRepository = communityMissionRewardRepository,
        ingredientRepository = ingredientRepository,
        userIngredientRepository = userIngredientRepository,
        ecoJamHistoryRepository = ecoJamHistoryRepository,
        ingredientHistoryRepository = ingredientHistoryRepository,
        communityMissionRewardRandomProvider = communityMissionRewardRandomProvider,
    )

    @Test
    fun `해금된 공동 미션은 완료 처리와 보상 지급을 함께 수행한다`() {
        val user = createUser(onboardingCompleted = true)
        val mission1 = createCommunityMission(
            id = 1L,
            difficulty = CommunityMissionDifficulty.ONE_STAR,
            stage = 1,
        )
        val mission2 = createCommunityMission(
            id = 2L,
            difficulty = CommunityMissionDifficulty.ONE_STAR,
            stage = 2,
        )
        val tomato = createIngredient(id = 10L, name = "토마토", type = IngredientType.COMMON)
        val onion = createIngredient(id = 11L, name = "양파", type = IngredientType.COMMON)
        val ecoJamReward = createCommunityMissionReward(
            id = 1L,
            communityMission = mission2,
            rewardType = CommunityMissionRewardType.ECO_JAM,
            ecoJamAmount = 50,
            rewardOrder = 1,
        )
        val ingredientReward = createCommunityMissionReward(
            id = 2L,
            communityMission = mission2,
            rewardType = CommunityMissionRewardType.INGREDIENT,
            ingredientType = IngredientType.COMMON,
            quantity = 2,
            ecoJamAmount = 0,
            rewardOrder = 2,
        )

        `when`(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user))
        `when`(communityMissionRepository.findAllByActiveTrue()).thenReturn(listOf(mission2, mission1))
        `when`(communityMissionCompletionRepository.findCompletedMissionIdsByUserId(1L)).thenReturn(listOf(1L))
        `when`(communityMissionCompletionRepository.existsByCommunityMissionIdAndUserId(2L, 1L)).thenReturn(false)
        `when`(communityMissionRewardRepository.findAllByCommunityMissionIdOrderByRewardOrderAsc(2L))
            .thenReturn(listOf(ecoJamReward, ingredientReward))
        `when`(ingredientRepository.findAllByType(IngredientType.COMMON)).thenReturn(listOf(tomato, onion))
        `when`(communityMissionRewardRandomProvider.nextInt(2)).thenReturn(0, 1)
        `when`(userIngredientRepository.findByUserIdAndIngredientId(1L, 10L)).thenReturn(null)
        `when`(userIngredientRepository.findByUserIdAndIngredientId(1L, 11L)).thenReturn(createUserIngredient(user = user, ingredient = onion, quantity = 3))
        `when`(communityMissionCompletionRepository.save(any(CommunityMissionCompletion::class.java))).thenAnswer { invocation ->
            val completion = invocation.arguments[0] as CommunityMissionCompletion
            CommunityMissionCompletion(
                id = 10L,
                communityMission = completion.communityMission,
                user = completion.user,
                completedAt = completion.completedAt,
            )
        }

        val response = communityMissionCompletionService.complete(1L, 2L)

        assertEquals(10L, response.completionId)
        assertEquals(2L, response.communityMissionId)
        assertEquals(50, response.rewardedEcoJam)
        assertEquals(2, response.rewardedIngredients.size)
        assertEquals("토마토", response.rewardedIngredients[0].ingredientName)
        assertEquals(1, response.rewardedIngredients[0].quantity)
        assertEquals("양파", response.rewardedIngredients[1].ingredientName)
        assertEquals(1, response.rewardedIngredients[1].quantity)
        assertEquals(50, user.ecoJam)
        val captor = ArgumentCaptor.forClass(CommunityMissionCompletion::class.java)
        verify(communityMissionCompletionRepository).save(captor.capture())
        assertEquals(2L, captor.value.communityMission.id)
        assertEquals(1L, captor.value.user.id)
        verify(ecoJamHistoryRepository).save(any())
        verify(ingredientHistoryRepository, times(2)).save(any())
        verify(userIngredientRepository, times(2)).save(any())
    }

    @Test
    fun `온보딩을 완료하지 않으면 공동 미션을 완료할 수 없다`() {
        val user = createUser(onboardingCompleted = false)

        `when`(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user))

        val exception = assertFailsWith<BusinessException> {
            communityMissionCompletionService.complete(1L, 1L)
        }

        assertEquals(ErrorCode.COMMUNITY_MISSION_ONBOARDING_REQUIRED, exception.errorCode)
        verify(communityMissionRepository, never()).findAllByActiveTrue()
    }

    @Test
    fun `이전 단계를 완료하지 않으면 잠긴 공동 미션을 완료할 수 없다`() {
        val user = createUser(onboardingCompleted = true)
        val mission1 = createCommunityMission(
            id = 1L,
            difficulty = CommunityMissionDifficulty.TWO_STAR,
            stage = 1,
        )
        val mission2 = createCommunityMission(
            id = 2L,
            difficulty = CommunityMissionDifficulty.TWO_STAR,
            stage = 2,
        )

        `when`(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user))
        `when`(communityMissionRepository.findAllByActiveTrue()).thenReturn(listOf(mission1, mission2))
        `when`(communityMissionCompletionRepository.findCompletedMissionIdsByUserId(1L)).thenReturn(emptyList())

        val exception = assertFailsWith<BusinessException> {
            communityMissionCompletionService.complete(1L, 2L)
        }

        assertEquals(ErrorCode.COMMUNITY_MISSION_NOT_UNLOCKED, exception.errorCode)
    }

    @Test
    fun `이미 완료한 공동 미션은 다시 완료할 수 없다`() {
        val user = createUser(onboardingCompleted = true)
        val mission = createCommunityMission(id = 1L)

        `when`(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user))
        `when`(communityMissionRepository.findAllByActiveTrue()).thenReturn(listOf(mission))
        `when`(communityMissionCompletionRepository.findCompletedMissionIdsByUserId(1L)).thenReturn(listOf(1L))
        `when`(communityMissionCompletionRepository.existsByCommunityMissionIdAndUserId(1L, 1L)).thenReturn(true)

        val exception = assertFailsWith<BusinessException> {
            communityMissionCompletionService.complete(1L, 1L)
        }

        assertEquals(ErrorCode.COMMUNITY_MISSION_ALREADY_COMPLETED, exception.errorCode)
    }

    @Test
    fun `공동 미션 보상 정보가 없으면 예외가 발생한다`() {
        val user = createUser(onboardingCompleted = true)
        val mission = createCommunityMission(id = 1L)

        `when`(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user))
        `when`(communityMissionRepository.findAllByActiveTrue()).thenReturn(listOf(mission))
        `when`(communityMissionCompletionRepository.findCompletedMissionIdsByUserId(1L)).thenReturn(emptyList())
        `when`(communityMissionCompletionRepository.existsByCommunityMissionIdAndUserId(1L, 1L)).thenReturn(false)
        `when`(communityMissionCompletionRepository.save(any(CommunityMissionCompletion::class.java))).thenReturn(
            CommunityMissionCompletion(
                id = 20L,
                communityMission = mission,
                user = user,
                completedAt = java.time.LocalDateTime.of(2026, 7, 21, 12, 0, 0),
            ),
        )
        `when`(communityMissionRewardRepository.findAllByCommunityMissionIdOrderByRewardOrderAsc(1L)).thenReturn(emptyList())

        val exception = assertFailsWith<BusinessException> {
            communityMissionCompletionService.complete(1L, 1L)
        }

        assertEquals(ErrorCode.COMMUNITY_MISSION_REWARD_NOT_FOUND, exception.errorCode)
    }
}
