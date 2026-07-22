package com.zerost.api.mission.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.ingredient.domain.IngredientRepository
import com.zerost.api.mission.domain.MissionCompletionRepository
import com.zerost.api.mission.domain.MissionCompletionStatus
import com.zerost.api.support.createIngredient
import com.zerost.api.support.createMission
import com.zerost.api.support.createMissionCompletion
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AdminMissionReviewServiceTest {

    private val missionCompletionRepository = mock(MissionCompletionRepository::class.java)
    private val ingredientRepository = mock(IngredientRepository::class.java)

    private val adminMissionReviewService = AdminMissionReviewService(
        missionCompletionRepository = missionCompletionRepository,
        ingredientRepository = ingredientRepository,
    )

    @Test
    fun `검수 대기 미션 인증을 승인하면 보상 재료를 확정한다`() {
        val mission = createMission(rewardIngredientPool = "[1]")
        val completion = createMissionCompletion(
            mission = mission,
            status = MissionCompletionStatus.PENDING,
        )
        val ingredient = createIngredient(id = 1L)

        `when`(missionCompletionRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(completion))
        `when`(ingredientRepository.findById(1L)).thenReturn(Optional.of(ingredient))

        val response = adminMissionReviewService.reviewMissionCompletion(1L, "APPROVED")

        assertEquals(1L, response.completionId)
        assertEquals("APPROVED", response.status)
        assertNotNull(completion.reviewedAt)
        assertEquals(ingredient, completion.rewardedIngredient)
    }

    @Test
    fun `검수 대기 미션 인증을 반려할 수 있다`() {
        val completion = createMissionCompletion(
            status = MissionCompletionStatus.PENDING,
        )
        `when`(missionCompletionRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(completion))

        val response = adminMissionReviewService.reviewMissionCompletion(1L, "REJECTED")

        assertEquals(1L, response.completionId)
        assertEquals("REJECTED", response.status)
        assertNotNull(completion.reviewedAt)
        verify(ingredientRepository, never()).findById(anyLong())
    }

    @Test
    fun `검수 대기가 아니면 승인할 수 없다`() {
        val completion = createMissionCompletion(
            status = MissionCompletionStatus.APPROVED,
        )
        `when`(missionCompletionRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(completion))

        val exception = assertThrows<BusinessException> {
            adminMissionReviewService.reviewMissionCompletion(1L, "APPROVED")
        }

        assertEquals(ErrorCode.INVALID_MISSION_REVIEW_STATUS, exception.errorCode)
    }

    @Test
    fun `미션 인증 정보가 없으면 예외가 발생한다`() {
        `when`(missionCompletionRepository.findByIdForUpdate(999L)).thenReturn(Optional.empty())

        val exception = assertThrows<BusinessException> {
            adminMissionReviewService.reviewMissionCompletion(999L, "APPROVED")
        }

        assertEquals(ErrorCode.MISSION_COMPLETION_NOT_FOUND, exception.errorCode)
    }

    @Test
    fun `지원하지 않는 검수 상태면 예외가 발생한다`() {
        val completion = createMissionCompletion(
            status = MissionCompletionStatus.PENDING,
        )
        `when`(missionCompletionRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(completion))

        val exception = assertThrows<BusinessException> {
            adminMissionReviewService.reviewMissionCompletion(1L, "DONE")
        }

        assertEquals(ErrorCode.INVALID_INPUT_VALUE, exception.errorCode)
    }

    @Test
    fun `보상 재료 설정이 잘못되면 예외가 발생한다`() {
        val completion = createMissionCompletion(
            mission = createMission(rewardIngredientPool = "[1, invalid]"),
            status = MissionCompletionStatus.PENDING,
        )

        `when`(missionCompletionRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(completion))

        val exception = assertThrows<BusinessException> {
            adminMissionReviewService.reviewMissionCompletion(1L, "APPROVED")
        }

        assertEquals(ErrorCode.INVALID_MISSION_REWARD_POOL, exception.errorCode)
        verify(ingredientRepository, never()).findById(anyLong())
    }
}
