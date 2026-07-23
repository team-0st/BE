package com.zerost.api.reward.application

import com.zerost.api.ingredient.domain.IngredientType
import com.zerost.api.mission.domain.MissionCompletionRepository
import com.zerost.api.mission.domain.MissionCompletionStatus
import com.zerost.api.support.createIngredient
import com.zerost.api.support.createMission
import com.zerost.api.support.createMissionCompletion
import com.zerost.api.support.createUser
import com.zerost.api.user.domain.UserRepository
import java.time.LocalDateTime
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import kotlin.test.assertEquals

class RewardQueryServiceTest {

    private val userRepository = mock(UserRepository::class.java)
    private val missionCompletionRepository = mock(MissionCompletionRepository::class.java)
    private val rewardQueryService = RewardQueryService(userRepository, missionCompletionRepository)

    @Test
    fun `승인되고 미수령인 미션 보상을 보상 탭 응답으로 변환한다`() {
        val user = createUser(id = 1L)
        val mission = createMission(id = 1L, title = "텀블러 사용 인증")
        val ingredient = createIngredient(id = 2L, name = "토마토", type = IngredientType.COMMON)
        val completion = createMissionCompletion(
            id = 55L,
            user = user,
            mission = mission,
            status = MissionCompletionStatus.APPROVED,
            rewardedIngredient = ingredient,
            reviewedAt = LocalDateTime.of(2026, 7, 22, 18, 0, 0),
            rewardClaimedAt = null,
        )

        `when`(userRepository.existsById(1L)).thenReturn(true)
        `when`(
            missionCompletionRepository
                .findAllByUserIdAndStatusAndRewardClaimedAtIsNullOrderByReviewedAtAscSubmittedAtAsc(
                    1L,
                    MissionCompletionStatus.APPROVED,
                ),
        ).thenReturn(listOf(completion))

        val response = rewardQueryService.getRewards(1L)

        assertEquals(1, response.summary.totalPendingRewardCount)
        assertEquals(1, response.summary.pendingCommonIngredientCount)
        assertEquals(0, response.summary.pendingHiddenIngredientCount)
        assertEquals(1, response.items.size)
        assertEquals(55L, response.items[0].rewardId)
        assertEquals(1L, response.items[0].sourceId)
        assertEquals("MISSION", response.items[0].rewardSourceType)
        assertEquals("텀블러 사용 인증", response.items[0].sourceTitle)
        assertEquals("INGREDIENT", response.items[0].rewards[0].rewardType)
        assertEquals("COMMON", response.items[0].rewards[0].ingredientType)
    }
}
