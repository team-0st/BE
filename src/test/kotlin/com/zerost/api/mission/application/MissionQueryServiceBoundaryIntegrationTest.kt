package com.zerost.api.mission.application

import com.zerost.api.mission.domain.DailyMissionSelection
import com.zerost.api.mission.domain.DailyMissionSelectionRepository
import com.zerost.api.mission.domain.Mission
import com.zerost.api.mission.domain.MissionCategory
import com.zerost.api.mission.domain.MissionCompletion
import com.zerost.api.mission.domain.MissionCompletionRepository
import com.zerost.api.mission.domain.MissionCompletionStatus
import com.zerost.api.mission.domain.MissionRepository
import com.zerost.api.support.createUser
import com.zerost.api.user.domain.UserRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertNull

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MissionQueryServiceBoundaryIntegrationTest(
    @Autowired private val missionQueryService: MissionQueryService,
    @Autowired private val userRepository: UserRepository,
    @Autowired private val missionRepository: MissionRepository,
    @Autowired private val missionCompletionRepository: MissionCompletionRepository,
    @Autowired private val dailyMissionSelectionRepository: DailyMissionSelectionRepository,
    @Autowired private val clock: Clock,
) {

    @Test
    fun `다음날 자정 제출 이력은 목록과 상세 모두 오늘 상태로 포함하지 않는다`() {
        val user = userRepository.save(createUser(id = null, onboardingCompleted = true))
        val generalMission = missionRepository.save(
            mission(title = "텀블러 사용하기", category = MissionCategory.GENERAL),
        )
        val secondGeneralMission = missionRepository.save(
            mission(title = "장바구니 사용하기", category = MissionCategory.GENERAL),
        )
        val thirdGeneralMission = missionRepository.save(
            mission(title = "분리배출", category = MissionCategory.GENERAL),
        )
        val specialMission = missionRepository.save(
            mission(title = "플로깅 인증", category = MissionCategory.SPECIAL),
        )

        val today = LocalDate.now(clock)
        val nextMidnight = today.plusDays(1).atStartOfDay()

        dailyMissionSelectionRepository.saveAll(
            listOf(
                DailyMissionSelection(
                    selectedDate = today,
                    mission = generalMission,
                    missionCategory = MissionCategory.GENERAL,
                    displayOrder = 1,
                ),
                DailyMissionSelection(
                    selectedDate = today,
                    mission = secondGeneralMission,
                    missionCategory = MissionCategory.GENERAL,
                    displayOrder = 2,
                ),
                DailyMissionSelection(
                    selectedDate = today,
                    mission = thirdGeneralMission,
                    missionCategory = MissionCategory.GENERAL,
                    displayOrder = 3,
                ),
                DailyMissionSelection(
                    selectedDate = today,
                    mission = specialMission,
                    missionCategory = MissionCategory.SPECIAL,
                    displayOrder = 1,
                ),
            ),
        )

        missionCompletionRepository.save(
            MissionCompletion(
                user = user,
                mission = generalMission,
                photoKey = "missions/${requireNotNull(user.id)}/${requireNotNull(generalMission.id)}/${nextMidnight.toLocalDate()}.jpg",
                status = MissionCompletionStatus.PENDING,
                submittedAt = nextMidnight,
            ),
        )

        val listResponse = missionQueryService.getMissions(requireNotNull(user.id))
        val detailResponse = missionQueryService.getMission(requireNotNull(user.id), requireNotNull(generalMission.id))

        assertEquals(3, listResponse.generalMissions.size)
        assertEquals(requireNotNull(generalMission.id), listResponse.generalMissions[0].id)
        assertNull(listResponse.generalMissions[0].todayStatus)
        assertNull(detailResponse.todayStatus)
        assertEquals(false, listResponse.generalMissions[0].rewardClaimable)
        assertEquals(false, detailResponse.rewardClaimable)
    }

    private fun mission(
        title: String,
        category: MissionCategory,
        rewardIngredientPool: String = "[1,2,3]",
    ): Mission = Mission(
        title = title,
        description = "$title 인증 미션",
        imageUrl = "https://example.com/${title.hashCode()}.png",
        missionCategory = category,
        rewardIngredientPool = rewardIngredientPool,
    )

    @TestConfiguration
    class FixedClockConfig {
        @Bean
        @Primary
        fun fixedClock(): Clock = Clock.fixed(
            Instant.parse("2026-08-04T01:00:00Z"),
            ZoneId.of("Asia/Seoul"),
        )
    }
}
