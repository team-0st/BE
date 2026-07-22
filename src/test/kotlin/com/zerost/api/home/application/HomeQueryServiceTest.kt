package com.zerost.api.home.application

import com.zerost.api.checkin.domain.CheckInRepository
import com.zerost.api.common.config.PublicAssetsProperties
import com.zerost.api.mission.domain.MissionCompletionRepository
import com.zerost.api.mission.domain.MissionCompletionStatus
import com.zerost.api.mission.domain.MissionRepository
import com.zerost.api.profile.application.ProfileCharacterImageUrlResolver
import com.zerost.api.support.createMission
import com.zerost.api.support.createMissionCompletion
import com.zerost.api.support.createUser
import com.zerost.api.user.domain.ProfileCharacterCode
import com.zerost.api.user.domain.UserRepository
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HomeQueryServiceTest {

    private val userRepository = mock(UserRepository::class.java)
    private val checkInRepository = mock(CheckInRepository::class.java)
    private val missionRepository = mock(MissionRepository::class.java)
    private val missionCompletionRepository = mock(MissionCompletionRepository::class.java)
    private val profileCharacterImageUrlResolver = ProfileCharacterImageUrlResolver(
        PublicAssetsProperties(
            baseUrl = "https://assets.zero-st.com",
        ),
    )
    private val homeQueryService = HomeQueryService(
        userRepository = userRepository,
        checkInRepository = checkInRepository,
        missionRepository = missionRepository,
        missionCompletionRepository = missionCompletionRepository,
        profileCharacterImageUrlResolver = profileCharacterImageUrlResolver,
    )

    @Test
    fun `홈 화면 조회 시 자산과 오늘 미션 진행 현황을 함께 반환한다`() {
        val user = createUser(
            nickname = "펭귄탐험가",
            profileCharacterCode = ProfileCharacterCode.BROCCOLI,
            ecoJam = 320,
            point = 1500,
        )
        val todayStart = LocalDate.now().atStartOfDay()
        val tomorrowStart = LocalDate.now().plusDays(1).atStartOfDay()
        val mission1 = createMission(id = 1L, title = "텀블러 사용")
        val mission2 = createMission(id = 2L, title = "장바구니 사용")
        val mission3 = createMission(id = 3L, title = "분리배출")
        val todayCompletions = listOf(
            createMissionCompletion(
                id = 1L,
                user = user,
                mission = mission1,
                status = MissionCompletionStatus.PENDING,
                submittedAt = LocalDateTime.now(),
            ),
            createMissionCompletion(
                id = 2L,
                user = user,
                mission = mission2,
                status = MissionCompletionStatus.APPROVED,
                submittedAt = LocalDateTime.now(),
            ),
            createMissionCompletion(
                id = 3L,
                user = user,
                mission = mission3,
                status = MissionCompletionStatus.REJECTED,
                submittedAt = LocalDateTime.now(),
            ),
        )

        `when`(userRepository.findById(1L)).thenReturn(Optional.of(user))
        `when`(checkInRepository.existsByUserIdAndCheckedDate(1L, LocalDate.now())).thenReturn(true)
        `when`(missionRepository.count()).thenReturn(7L)
        `when`(
            missionCompletionRepository.findAllByUserIdAndSubmittedAtGreaterThanEqualAndSubmittedAtLessThan(
                1L,
                todayStart,
                tomorrowStart,
            ),
        ).thenReturn(todayCompletions)

        val response = homeQueryService.getHome(1L)

        assertEquals("펭귄탐험가", response.nickname)
        assertEquals("BROCCOLI", response.profileCharacterCode)
        assertEquals("https://assets.zero-st.com/profile-characters/broccoli.png", response.profileCharacterImageUrl)
        assertEquals(320, response.ecoJam)
        assertEquals(1500, response.point)
        assertTrue(response.checkedInToday)
        assertEquals(7, response.missionProgress.totalMissionCount)
        assertEquals(3, response.missionProgress.submittedMissionCount)
        assertEquals(1, response.missionProgress.pendingMissionCount)
        assertEquals(1, response.missionProgress.approvedMissionCount)
        assertEquals(1, response.missionProgress.rejectedMissionCount)
    }

    @Test
    fun `홈 화면 조회는 다음날 자정 이전까지만 오늘 제출 내역으로 집계한다`() {
        val user = createUser()
        val todayStart = LocalDate.now().atStartOfDay()
        val tomorrowStart = LocalDate.now().plusDays(1).atStartOfDay()

        `when`(userRepository.findById(1L)).thenReturn(Optional.of(user))
        `when`(checkInRepository.existsByUserIdAndCheckedDate(1L, LocalDate.now())).thenReturn(false)
        `when`(missionRepository.count()).thenReturn(7L)
        `when`(
            missionCompletionRepository.findAllByUserIdAndSubmittedAtGreaterThanEqualAndSubmittedAtLessThan(
                1L,
                todayStart,
                tomorrowStart,
            ),
        ).thenReturn(emptyList())

        val response = homeQueryService.getHome(1L)

        assertEquals(0, response.missionProgress.submittedMissionCount)
    }
}
