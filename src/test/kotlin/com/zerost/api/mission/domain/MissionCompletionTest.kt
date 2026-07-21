package com.zerost.api.mission.domain

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.support.createMission
import com.zerost.api.support.createMissionCompletion
import com.zerost.api.support.createUser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MissionCompletionTest {

    @Test
    fun `미션 제출 생성 시 기본 상태는 검수 대기다`() {
        val completion = MissionCompletion.submit(
            user = createUser(),
            mission = createMission(),
            photoKey = "missions/1/1/2026/07/18/mission-1.jpg",
            submittedAt = java.time.LocalDateTime.of(2026, 7, 17, 10, 0, 0),
        )

        assertEquals(MissionCompletionStatus.PENDING, completion.status)
        assertEquals("missions/1/1/2026/07/18/mission-1.jpg", completion.photoKey)
    }

    @Test
    fun `검수 대기 상태면 승인할 수 있다`() {
        val completion = createMissionCompletion(
            status = MissionCompletionStatus.PENDING,
        )

        val reviewedAt = java.time.LocalDateTime.of(2026, 7, 19, 14, 30, 0)
        completion.approve(reviewedAt)

        assertEquals(MissionCompletionStatus.APPROVED, completion.status)
        assertEquals(reviewedAt, completion.reviewedAt)
    }

    @Test
    fun `검수 대기 상태가 아니면 승인할 수 없다`() {
        val completion = createMissionCompletion(
            status = MissionCompletionStatus.REJECTED,
        )

        val exception = assertFailsWith<BusinessException> {
            completion.approve(java.time.LocalDateTime.of(2026, 7, 19, 14, 30, 0))
        }

        assertEquals(ErrorCode.INVALID_MISSION_REVIEW_STATUS, exception.errorCode)
    }

    @Test
    fun `검수 대기 상태면 인증 이미지를 수정할 수 있다`() {
        val completion = createMissionCompletion(
            photoKey = "missions/1/1/2026/07/18/old.jpg",
            status = MissionCompletionStatus.PENDING,
        )

        completion.updatePhotoKey("missions/1/1/2026/07/18/new.jpg")

        assertEquals("missions/1/1/2026/07/18/new.jpg", completion.photoKey)
    }

    @Test
    fun `승인된 미션 인증은 수정할 수 없다`() {
        val completion = createMissionCompletion(
            status = MissionCompletionStatus.APPROVED,
        )

        val exception = assertFailsWith<BusinessException> {
            completion.updatePhotoKey("missions/1/1/2026/07/18/new.jpg")
        }

        assertEquals(ErrorCode.MISSION_COMPLETION_MODIFICATION_NOT_ALLOWED, exception.errorCode)
    }
}
