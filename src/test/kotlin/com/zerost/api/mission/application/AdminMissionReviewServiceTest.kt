package com.zerost.api.mission.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.mission.domain.MissionCompletionRepository
import com.zerost.api.mission.domain.MissionCompletionStatus
import com.zerost.api.support.createMissionCompletion
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AdminMissionReviewServiceTest {

    private val missionCompletionRepository = mock(MissionCompletionRepository::class.java)
    private val adminMissionReviewService = AdminMissionReviewService(missionCompletionRepository)

    @Test
    fun `검수 대기 미션 인증을 승인할 수 있다`() {
        val completion = createMissionCompletion(
            status = MissionCompletionStatus.PENDING,
        )
        `when`(missionCompletionRepository.findById(1L)).thenReturn(Optional.of(completion))

        val response = adminMissionReviewService.reviewMissionCompletion(1L, "APPROVED")

        assertEquals(1L, response.completionId)
        assertEquals("APPROVED", response.status)
        assertNotNull(completion.reviewedAt)
    }

    @Test
    fun `검수 대기 미션 인증을 반려할 수 있다`() {
        val completion = createMissionCompletion(
            status = MissionCompletionStatus.PENDING,
        )
        `when`(missionCompletionRepository.findById(1L)).thenReturn(Optional.of(completion))

        val response = adminMissionReviewService.reviewMissionCompletion(1L, "REJECTED")

        assertEquals(1L, response.completionId)
        assertEquals("REJECTED", response.status)
        assertNotNull(completion.reviewedAt)
    }

    @Test
    fun `검수 대기가 아니면 승인할 수 없다`() {
        val completion = createMissionCompletion(
            status = MissionCompletionStatus.APPROVED,
        )
        `when`(missionCompletionRepository.findById(1L)).thenReturn(Optional.of(completion))

        val exception = assertThrows<BusinessException> {
            adminMissionReviewService.reviewMissionCompletion(1L, "APPROVED")
        }

        assertEquals(ErrorCode.INVALID_MISSION_REVIEW_STATUS, exception.errorCode)
    }

    @Test
    fun `지원하지 않는 검수 상태면 예외가 발생한다`() {
        val completion = createMissionCompletion(
            status = MissionCompletionStatus.PENDING,
        )
        `when`(missionCompletionRepository.findById(1L)).thenReturn(Optional.of(completion))

        val exception = assertThrows<BusinessException> {
            adminMissionReviewService.reviewMissionCompletion(1L, "DONE")
        }

        assertEquals(ErrorCode.INVALID_INPUT_VALUE, exception.errorCode)
    }
}
