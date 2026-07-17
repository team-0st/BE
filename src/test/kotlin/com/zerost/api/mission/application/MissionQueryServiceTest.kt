package com.zerost.api.mission.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.mission.domain.MissionCompletionRepository
import com.zerost.api.mission.domain.MissionCompletionStatus
import com.zerost.api.mission.domain.MissionRepository
import com.zerost.api.support.createIngredient
import com.zerost.api.support.createMission
import com.zerost.api.support.createMissionCompletion
import com.zerost.api.support.createUser
import com.zerost.api.user.domain.UserRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MissionQueryServiceTest {

    private val userRepository = mock(UserRepository::class.java)
    private val missionRepository = mock(MissionRepository::class.java)
    private val missionCompletionRepository = mock(MissionCompletionRepository::class.java)
    private val missionQueryService = MissionQueryService(
        userRepository = userRepository,
        missionRepository = missionRepository,
        missionCompletionRepository = missionCompletionRepository,
    )

    @Test
    fun `미션 목록 조회 시 오늘 상태를 함께 반환한다`() {
        val user = createUser()
        val mission1 = createMission(id = 1L, title = "텀블러 사용하기")
        val mission2 = createMission(id = 2L, title = "장바구니 지참하기")
        val (start, end) = todayRange()
        val completion = createMissionCompletion(
            mission = mission1,
            status = MissionCompletionStatus.PENDING,
        )
        `when`(userRepository.findByDeviceId("device-1")).thenReturn(Optional.of(user))
        `when`(missionRepository.findAll()).thenReturn(listOf(mission1, mission2))
        `when`(
            missionCompletionRepository.findTopByUserIdAndMissionIdAndSubmittedAtBetweenOrderBySubmittedAtDesc(
                1L,
                1L,
                start,
                end,
            ),
        ).thenReturn(completion)
        `when`(
            missionCompletionRepository.findTopByUserIdAndMissionIdAndSubmittedAtBetweenOrderBySubmittedAtDesc(
                1L,
                2L,
                start,
                end,
            ),
        ).thenReturn(null)

        val response = missionQueryService.getMissions("device-1")

        assertEquals(2, response.size)
        assertEquals("텀블러 사용하기", response[0].title)
        assertEquals("PENDING", response[0].todayStatus?.name)
        assertNull(response[1].todayStatus)
    }

    @Test
    fun `미션 상세 조회 시 오늘 상태를 반환한다`() {
        val user = createUser()
        val mission = createMission(id = 1L)
        val (start, end) = todayRange()
        val completion = createMissionCompletion(
            mission = mission,
            status = MissionCompletionStatus.APPROVED,
        )
        `when`(userRepository.findByDeviceId("device-1")).thenReturn(Optional.of(user))
        `when`(missionRepository.findById(1L)).thenReturn(Optional.of(mission))
        `when`(
            missionCompletionRepository.findTopByUserIdAndMissionIdAndSubmittedAtBetweenOrderBySubmittedAtDesc(
                1L,
                1L,
                start,
                end,
            ),
        ).thenReturn(completion)

        val response = missionQueryService.getMission("device-1", 1L)

        assertEquals(1L, response.id)
        assertEquals("APPROVED", response.todayStatus?.name)
    }

    @Test
    fun `미션 내역 조회 시 보상 재료 정보를 함께 반환한다`() {
        val user = createUser()
        val mission = createMission(id = 1L)
        val ingredient = createIngredient(id = 5L, name = "낡은 밧줄", imageUrl = "image-5")
        val completion = createMissionCompletion(
            id = 55L,
            mission = mission,
            status = MissionCompletionStatus.APPROVED,
            rewardedIngredient = ingredient,
            reviewedAt = LocalDateTime.of(2026, 7, 17, 14, 0, 0),
        )
        `when`(userRepository.findByDeviceId("device-1")).thenReturn(Optional.of(user))
        `when`(missionCompletionRepository.findAllByUserIdOrderBySubmittedAtDesc(1L)).thenReturn(listOf(completion))

        val response = missionQueryService.getMissionCompletions("device-1")

        assertEquals(1, response.size)
        assertEquals(55L, response[0].completionId)
        assertEquals("텀블러 사용하기", response[0].missionTitle)
        assertEquals(5L, response[0].rewardedIngredient?.id)
    }

    @Test
    fun `없는 미션 상세 조회 시 예외가 발생한다`() {
        `when`(userRepository.findByDeviceId("device-1")).thenReturn(Optional.of(createUser()))
        `when`(missionRepository.findById(999L)).thenReturn(Optional.empty())

        val exception = assertThrows<BusinessException> {
            missionQueryService.getMission("device-1", 999L)
        }

        assertEquals(ErrorCode.MISSION_NOT_FOUND, exception.errorCode)
    }

    private fun todayRange(): Pair<LocalDateTime, LocalDateTime> {
        val today = LocalDate.now()
        return today.atStartOfDay() to today.plusDays(1).atStartOfDay()
    }
}
