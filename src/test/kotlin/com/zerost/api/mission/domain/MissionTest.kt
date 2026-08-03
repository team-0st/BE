package com.zerost.api.mission.domain

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.support.createMission
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MissionTest {

    @Test
    fun `보상 재료 ID 목록을 파싱할 수 있다`() {
        val mission = createMission(rewardIngredientPool = "[1, 2, 3]")

        val rewardIngredientIds = mission.extractRewardIngredientIds()

        assertEquals(listOf(1L, 2L, 3L), rewardIngredientIds)
    }

    @Test
    fun `보상 재료 설정에 잘못된 값이 있으면 예외가 발생한다`() {
        val mission = createMission(rewardIngredientPool = "[1, invalid]")

        val exception = assertFailsWith<BusinessException> {
            mission.extractRewardIngredientIds()
        }

        assertEquals(ErrorCode.INVALID_MISSION_REWARD_POOL, exception.errorCode)
    }

    @Test
    fun `보상 재료 풀이 하나면 해당 재료를 그대로 선택한다`() {
        val mission = createMission(rewardIngredientPool = "[7]")

        val rewardIngredientId = mission.pickRewardIngredientId()

        assertEquals(7L, rewardIngredientId)
    }
}
