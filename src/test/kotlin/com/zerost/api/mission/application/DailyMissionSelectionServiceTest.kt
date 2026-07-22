package com.zerost.api.mission.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.mission.domain.DailyMissionSelection
import com.zerost.api.mission.domain.DailyMissionSelectionRepository
import com.zerost.api.mission.domain.MissionCategory
import com.zerost.api.support.createMission
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.springframework.dao.DataIntegrityViolationException
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.test.assertEquals

class DailyMissionSelectionServiceTest {

    private val dailyMissionSelectionRepository = mock(DailyMissionSelectionRepository::class.java)
    private val dailyMissionSelectionProvisionService = mock(DailyMissionSelectionProvisionService::class.java)
    private val clock = Clock.fixed(Instant.parse("2026-07-22T00:00:00Z"), ZoneId.of("Asia/Seoul"))
    private val dailyMissionSelectionService = DailyMissionSelectionService(
        dailyMissionSelectionRepository = dailyMissionSelectionRepository,
        dailyMissionSelectionProvisionService = dailyMissionSelectionProvisionService,
        clock = clock,
    )

    @Test
    fun `오늘 편성이 이미 존재하면 기존 편성을 반환한다`() {
        val today = LocalDate.now(clock)
        val general1 = createMission(id = 1L, title = "텀블러 사용하기", missionCategory = MissionCategory.GENERAL)
        val general2 = createMission(id = 2L, title = "장바구니 지참하기", missionCategory = MissionCategory.GENERAL)
        val general3 = createMission(id = 3L, title = "분리배출", missionCategory = MissionCategory.GENERAL)
        val special = createMission(id = 4L, title = "플로깅 인증", missionCategory = MissionCategory.SPECIAL)
        `when`(
            dailyMissionSelectionRepository.findAllBySelectedDateAndMissionCategoryOrderByDisplayOrderAsc(
                today,
                MissionCategory.GENERAL,
            ),
        ).thenReturn(
            listOf(
                DailyMissionSelection(selectedDate = today, mission = general1, missionCategory = MissionCategory.GENERAL, displayOrder = 1),
                DailyMissionSelection(selectedDate = today, mission = general2, missionCategory = MissionCategory.GENERAL, displayOrder = 2),
                DailyMissionSelection(selectedDate = today, mission = general3, missionCategory = MissionCategory.GENERAL, displayOrder = 3),
            ),
        )
        `when`(
            dailyMissionSelectionRepository.findAllBySelectedDateAndMissionCategoryOrderByDisplayOrderAsc(
                today,
                MissionCategory.SPECIAL,
            ),
        ).thenReturn(
            listOf(
                DailyMissionSelection(selectedDate = today, mission = special, missionCategory = MissionCategory.SPECIAL, displayOrder = 1),
            ),
        )

        val response = dailyMissionSelectionService.getTodaySelections()

        assertEquals(3, response.generalMissions.size)
        assertEquals("플로깅 인증", response.specialMission.title)
        verifyNoInteractions(dailyMissionSelectionProvisionService)
    }

    @Test
    fun `오늘 편성이 없으면 일반 3개와 특별 1개를 선택해 저장한다`() {
        val today = LocalDate.now(clock)
        val general1 = createMission(id = 1L, title = "텀블러 사용하기", missionCategory = MissionCategory.GENERAL)
        val general2 = createMission(id = 2L, title = "장바구니 지참하기", missionCategory = MissionCategory.GENERAL)
        val general3 = createMission(id = 3L, title = "분리배출", missionCategory = MissionCategory.GENERAL)
        `when`(
            dailyMissionSelectionRepository.findAllBySelectedDateAndMissionCategoryOrderByDisplayOrderAsc(
                today,
                MissionCategory.GENERAL,
            ),
        ).thenReturn(emptyList())
        `when`(
            dailyMissionSelectionRepository.findAllBySelectedDateAndMissionCategoryOrderByDisplayOrderAsc(
                today,
                MissionCategory.SPECIAL,
            ),
        ).thenReturn(emptyList())
        `when`(dailyMissionSelectionProvisionService.create(today)).thenReturn(
            DailyMissionSelections(
                generalMissions = listOf(general1, general2, general3),
                specialMission = createMission(id = 6L, title = "제로웨이스트샵 방문", missionCategory = MissionCategory.SPECIAL),
            ),
        )

        val response = dailyMissionSelectionService.getTodaySelections()

        assertEquals(3, response.generalMissions.size)
        assertEquals("텀블러 사용하기", response.generalMissions[0].title)
        assertEquals("장바구니 지참하기", response.generalMissions[1].title)
        assertEquals("분리배출", response.generalMissions[2].title)
        assertEquals("제로웨이스트샵 방문", response.specialMission.title)
        verify(dailyMissionSelectionProvisionService).create(today)
    }

    @Test
    fun `편성 가능한 미션 수가 부족하면 예외가 발생한다`() {
        val today = LocalDate.now(clock)
        `when`(
            dailyMissionSelectionRepository.findAllBySelectedDateAndMissionCategoryOrderByDisplayOrderAsc(
                today,
                MissionCategory.GENERAL,
            ),
        ).thenReturn(emptyList())
        `when`(
            dailyMissionSelectionRepository.findAllBySelectedDateAndMissionCategoryOrderByDisplayOrderAsc(
                today,
                MissionCategory.SPECIAL,
            ),
        ).thenReturn(emptyList())
        `when`(dailyMissionSelectionProvisionService.create(today)).thenThrow(
            BusinessException(ErrorCode.INVALID_DAILY_MISSION_SELECTION),
        )

        val exception = assertThrows<BusinessException> {
            dailyMissionSelectionService.getTodaySelections()
        }

        assertEquals(ErrorCode.INVALID_DAILY_MISSION_SELECTION, exception.errorCode)
    }

    @Test
    fun `편성 저장 충돌이 나면 트랜잭션 바깥에서 기존 편성을 다시 조회한다`() {
        val today = LocalDate.now(clock)
        val general1 = createMission(id = 1L, title = "텀블러 사용하기", missionCategory = MissionCategory.GENERAL)
        val general2 = createMission(id = 2L, title = "장바구니 지참하기", missionCategory = MissionCategory.GENERAL)
        val general3 = createMission(id = 3L, title = "분리배출", missionCategory = MissionCategory.GENERAL)
        val special = createMission(id = 4L, title = "플로깅 인증", missionCategory = MissionCategory.SPECIAL)
        `when`(
            dailyMissionSelectionRepository.findAllBySelectedDateAndMissionCategoryOrderByDisplayOrderAsc(
                today,
                MissionCategory.GENERAL,
            ),
        ).thenReturn(emptyList())
            .thenReturn(
                listOf(
                    DailyMissionSelection(selectedDate = today, mission = general1, missionCategory = MissionCategory.GENERAL, displayOrder = 1),
                    DailyMissionSelection(selectedDate = today, mission = general2, missionCategory = MissionCategory.GENERAL, displayOrder = 2),
                    DailyMissionSelection(selectedDate = today, mission = general3, missionCategory = MissionCategory.GENERAL, displayOrder = 3),
                ),
            )
        `when`(
            dailyMissionSelectionRepository.findAllBySelectedDateAndMissionCategoryOrderByDisplayOrderAsc(
                today,
                MissionCategory.SPECIAL,
            ),
        ).thenReturn(emptyList())
            .thenReturn(
                listOf(
                    DailyMissionSelection(selectedDate = today, mission = special, missionCategory = MissionCategory.SPECIAL, displayOrder = 1),
                ),
            )
        `when`(dailyMissionSelectionProvisionService.create(today)).thenThrow(DataIntegrityViolationException("duplicate"))

        val response = dailyMissionSelectionService.getTodaySelections()

        assertEquals(3, response.generalMissions.size)
        assertEquals("플로깅 인증", response.specialMission.title)
    }
}
