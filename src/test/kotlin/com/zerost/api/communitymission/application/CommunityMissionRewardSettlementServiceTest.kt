package com.zerost.api.communitymission.application

import com.zerost.api.communitymission.domain.CommunityMissionCompletionRepository
import com.zerost.api.communitymission.domain.CommunityMissionRewardRepository
import com.zerost.api.communitymission.domain.CommunityMissionRewardType
import com.zerost.api.ecojam.domain.EcoJamHistoryRepository
import com.zerost.api.ingredient.domain.IngredientHistoryRepository
import com.zerost.api.ingredient.domain.IngredientRepository
import com.zerost.api.ingredient.domain.IngredientType
import com.zerost.api.ingredient.domain.UserIngredientRepository
import com.zerost.api.support.createCommunityMission
import com.zerost.api.support.createCommunityMissionCompletion
import com.zerost.api.support.createCommunityMissionReward
import com.zerost.api.support.createIngredient
import com.zerost.api.support.createUser
import com.zerost.api.support.createUserIngredient
import com.zerost.api.user.domain.UserRepository
import org.junit.jupiter.api.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CommunityMissionRewardSettlementServiceTest {

    private val communityMissionCompletionRepository = mock(CommunityMissionCompletionRepository::class.java)
    private val communityMissionRewardRepository = mock(CommunityMissionRewardRepository::class.java)
    private val ingredientRepository = mock(IngredientRepository::class.java)
    private val userIngredientRepository = mock(UserIngredientRepository::class.java)
    private val ecoJamHistoryRepository = mock(EcoJamHistoryRepository::class.java)
    private val ingredientHistoryRepository = mock(IngredientHistoryRepository::class.java)
    private val userRepository = mock(UserRepository::class.java)
    private val communityMissionRewardRandomProvider = mock(CommunityMissionRewardRandomProvider::class.java)
    private val communityMissionRewardSettlementService = CommunityMissionRewardSettlementService(
        communityMissionCompletionRepository = communityMissionCompletionRepository,
        communityMissionRewardRepository = communityMissionRewardRepository,
        ingredientRepository = ingredientRepository,
        userIngredientRepository = userIngredientRepository,
        ecoJamHistoryRepository = ecoJamHistoryRepository,
        ingredientHistoryRepository = ingredientHistoryRepository,
        userRepository = userRepository,
        communityMissionRewardRandomProvider = communityMissionRewardRandomProvider,
    )

    @Test
    fun `공동미션 성공 정산은 미지급 완료자를 배치로 나누어 처리한다`() {
        val mission = createCommunityMission(id = 1L)
        val firstUser = createUser(id = 1L, onboardingCompleted = true)
        val secondUser = createUser(id = 2L, onboardingCompleted = true)
        val firstCompletion = createCommunityMissionCompletion(id = 10L, communityMission = mission, user = firstUser)
        val secondCompletion = createCommunityMissionCompletion(id = 11L, communityMission = mission, user = secondUser)
        val reward = createCommunityMissionReward(
            id = 1L,
            communityMission = mission,
            rewardType = CommunityMissionRewardType.ECO_JAM,
            ecoJamAmount = 50,
            rewardOrder = 1,
        )

        `when`(communityMissionRewardRepository.findAllByCommunityMissionIdOrderByRewardOrderAsc(1L))
            .thenReturn(listOf(reward))
        `when`(communityMissionCompletionRepository.findTop100ByCommunityMissionIdAndRewardedAtIsNullOrderByIdAsc(1L))
            .thenReturn(listOf(firstCompletion, secondCompletion))
            .thenReturn(emptyList())
        `when`(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(firstUser))
        `when`(userRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(secondUser))

        val rewardedAt = LocalDateTime.of(2026, 7, 21, 23, 0, 0)
        communityMissionRewardSettlementService.settlePendingRewards(1L, rewardedAt)

        assertEquals(50, firstUser.ecoJam)
        assertEquals(50, secondUser.ecoJam)
        assertTrue(firstCompletion.isRewarded())
        assertTrue(secondCompletion.isRewarded())
        verify(communityMissionCompletionRepository, times(2))
            .findTop100ByCommunityMissionIdAndRewardedAtIsNullOrderByIdAsc(1L)
        verify(ecoJamHistoryRepository, times(2)).save(any())
    }

    @Test
    fun `재료 수량이 여러 개여도 재료 목록은 타입별로 한 번만 조회한다`() {
        val user = createUser(id = 1L, onboardingCompleted = true)
        val mission = createCommunityMission(id = 1L)
        val completion = createCommunityMissionCompletion(id = 20L, communityMission = mission, user = user)
        val tomato = createIngredient(id = 100L, name = "토마토", type = IngredientType.COMMON)
        val onion = createIngredient(id = 101L, name = "양파", type = IngredientType.COMMON)
        val ecoJamReward = createCommunityMissionReward(
            id = 1L,
            communityMission = mission,
            rewardType = CommunityMissionRewardType.ECO_JAM,
            ecoJamAmount = 50,
            rewardOrder = 1,
        )
        val ingredientReward = createCommunityMissionReward(
            id = 2L,
            communityMission = mission,
            rewardType = CommunityMissionRewardType.INGREDIENT,
            ingredientType = IngredientType.COMMON,
            quantity = 2,
            ecoJamAmount = 0,
            rewardOrder = 2,
        )

        `when`(communityMissionRewardRepository.findAllByCommunityMissionIdOrderByRewardOrderAsc(1L))
            .thenReturn(listOf(ecoJamReward, ingredientReward))
        `when`(ingredientRepository.findAllByType(IngredientType.COMMON)).thenReturn(listOf(tomato, onion))
        `when`(communityMissionRewardRandomProvider.nextInt(2)).thenReturn(0, 1)
        `when`(userIngredientRepository.findByUserIdAndIngredientId(1L, 100L)).thenReturn(null)
        `when`(userIngredientRepository.findByUserIdAndIngredientId(1L, 101L))
            .thenReturn(createUserIngredient(user = user, ingredient = onion, quantity = 0))

        val rewardResult = communityMissionRewardSettlementService.rewardCurrentCompletion(
            completion = completion,
            user = user,
            rewardedAt = LocalDateTime.of(2026, 7, 21, 23, 30, 0),
        )

        assertEquals(50, rewardResult.rewardedEcoJam)
        assertEquals(2, rewardResult.rewardedIngredients.size)
        assertTrue(completion.isRewarded())
        verify(ingredientRepository, times(1)).findAllByType(IngredientType.COMMON)
        verify(ingredientHistoryRepository, times(2)).save(any())
    }
}
