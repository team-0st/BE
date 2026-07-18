package com.zerost.api.mission.domain

import com.zerost.api.support.createMission
import com.zerost.api.support.createUser
import kotlin.test.Test
import kotlin.test.assertEquals

class MissionCompletionTest {

    @Test
    fun `미션 제출 생성 시 기본 상태는 검수 대기다`() {
        val completion = MissionCompletion.submit(
            user = createUser(),
            mission = createMission(),
            photoKey = "missions/device-1/2026/07/18/mission-1.jpg",
            submittedAt = java.time.LocalDateTime.of(2026, 7, 17, 10, 0, 0),
        )

        assertEquals(MissionCompletionStatus.PENDING, completion.status)
        assertEquals("missions/device-1/2026/07/18/mission-1.jpg", completion.photoKey)
    }
}
