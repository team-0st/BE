package com.zerost.api.mission.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.ingredient.domain.IngredientHistoryRepository
import com.zerost.api.ingredient.domain.UserIngredient
import com.zerost.api.ingredient.domain.UserIngredientRepository
import com.zerost.api.mission.domain.MissionCompletionRepository
import com.zerost.api.mission.domain.MissionCompletionStatus
import com.zerost.api.support.createIngredient
import com.zerost.api.support.createMissionCompletion
import com.zerost.api.support.createUser
import com.zerost.api.user.domain.UserRepository
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class MissionRewardClaimServiceTest {

    private val missionCompletionRepository = mock(MissionCompletionRepository::class.java)
    private val userRepository = mock(UserRepository::class.java)
    private val userIngredientRepository = mock(UserIngredientRepository::class.java)
    private val ingredientHistoryRepository = mock(IngredientHistoryRepository::class.java)

    private val missionRewardClaimService = MissionRewardClaimService(
        missionCompletionRepository = missionCompletionRepository,
        userRepository = userRepository,
        userIngredientRepository = userIngredientRepository,
        ingredientHistoryRepository = ingredientHistoryRepository,
    )

    @Test
    fun `승인된 미션 보상을 수령하면 재료와 이력을 적재한다`() {
        val user = createUser()
        val ingredient = createIngredient(id = 3L, name = "당근")
        val completion = createMissionCompletion(
            user = user,
            status = MissionCompletionStatus.APPROVED,
            rewardedIngredient = ingredient,
        )

        `when`(missionCompletionRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(completion))
        `when`(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user))
        `when`(userIngredientRepository.findByUserAndIngredient(user, ingredient)).thenReturn(Optional.empty())
        `when`(userIngredientRepository.save(any(UserIngredient::class.java))).thenAnswer { it.arguments[0] as UserIngredient }

        val response = missionRewardClaimService.claimReward(1L, 1L)

        assertEquals(1L, response.completionId)
        assertEquals(1L, response.missionId)
        assertEquals(3L, response.rewardedIngredient.id)
        assertNotNull(completion.rewardClaimedAt)
        verify(userIngredientRepository).save(any(UserIngredient::class.java))
        verify(ingredientHistoryRepository).save(any())
    }

    @Test
    fun `승인되지 않은 미션 보상은 수령할 수 없다`() {
        val completion = createMissionCompletion(
            status = MissionCompletionStatus.PENDING,
            rewardedIngredient = createIngredient(id = 3L),
        )
        `when`(missionCompletionRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(completion))

        val exception = assertThrows<BusinessException> {
            missionRewardClaimService.claimReward(1L, 1L)
        }

        assertEquals(ErrorCode.MISSION_REWARD_CLAIM_NOT_AVAILABLE, exception.errorCode)
        verify(userIngredientRepository, never()).save(any())
    }

    @Test
    fun `이미 수령한 미션 보상은 다시 수령할 수 없다`() {
        val completion = createMissionCompletion(
            status = MissionCompletionStatus.APPROVED,
            rewardedIngredient = createIngredient(id = 3L),
            rewardClaimedAt = java.time.LocalDateTime.of(2026, 7, 22, 19, 0, 0),
        )
        `when`(missionCompletionRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(completion))

        val exception = assertThrows<BusinessException> {
            missionRewardClaimService.claimReward(1L, 1L)
        }

        assertEquals(ErrorCode.MISSION_REWARD_ALREADY_CLAIMED, exception.errorCode)
        verify(userIngredientRepository, never()).save(any())
    }
}
