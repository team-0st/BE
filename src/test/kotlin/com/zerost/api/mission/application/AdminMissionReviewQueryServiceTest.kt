package com.zerost.api.mission.application

import com.zerost.api.mission.domain.MissionCompletionRepository
import com.zerost.api.mission.domain.MissionCompletionStatus
import com.zerost.api.support.createMission
import com.zerost.api.support.createMissionCompletion
import com.zerost.api.support.createUser
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import kotlin.test.assertEquals

class AdminMissionReviewQueryServiceTest {

    private val missionCompletionRepository = mock(MissionCompletionRepository::class.java)
    private val adminMissionReviewQueryService = AdminMissionReviewQueryService(missionCompletionRepository)

    @Test
    fun `검수 대기 미션 인증 목록을 관리자 응답으로 변환한다`() {
        val user = createUser(id = 3L, nickname = "펭귄탐험가")
        val mission = createMission(id = 1L, title = "텀블러 사용하기")
        val completion = createMissionCompletion(
            id = 12L,
            user = user,
            mission = mission,
            status = MissionCompletionStatus.PENDING,
        )
        `when`(missionCompletionRepository.findAllByStatusOrderBySubmittedAtAsc(MissionCompletionStatus.PENDING))
            .thenReturn(listOf(completion))

        val response = adminMissionReviewQueryService.getPendingMissionCompletions()

        assertEquals(1, response.size)
        assertEquals(12L, response[0].completionId)
        assertEquals(3L, response[0].userId)
        assertEquals("펭귄탐험가", response[0].userNickname)
        assertEquals(1L, response[0].missionId)
        assertEquals("텀블러 사용하기", response[0].missionTitle)
        assertEquals("missions/device-1/1/2026/07/18/mission-1.jpg", response[0].photoKey)
    }
}
