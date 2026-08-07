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
import jakarta.persistence.EntityManager
import jakarta.persistence.EntityManagerFactory
import org.hibernate.SessionFactory
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.system.measureNanoTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(
    properties = [
        "spring.jpa.properties.hibernate.generate_statistics=true",
    ],
)
class MissionQueryServiceMeasurementTest(
    @Autowired private val missionQueryService: MissionQueryService,
    @Autowired private val userRepository: UserRepository,
    @Autowired private val missionRepository: MissionRepository,
    @Autowired private val missionCompletionRepository: MissionCompletionRepository,
    @Autowired private val dailyMissionSelectionRepository: DailyMissionSelectionRepository,
    @Autowired private val entityManagerFactory: EntityManagerFactory,
    @Autowired private val entityManager: EntityManager,
    @Autowired private val clock: Clock,
) {

    @Test
    fun `미션 목록 조회 baseline SQL 수와 단건 시간을 측정한다`() {
        val user = userRepository.save(createUser(id = null, onboardingCompleted = true))

        val generalMissions = missionRepository.saveAll(
            listOf(
                mission(title = "텀블러 사용", category = MissionCategory.GENERAL),
                mission(title = "장바구니 사용", category = MissionCategory.GENERAL),
                mission(title = "분리배출", category = MissionCategory.GENERAL),
            ),
        )
        val specialMission = missionRepository.save(
            mission(title = "플로깅", category = MissionCategory.SPECIAL, rewardIngredientPool = "[8]"),
        )

        val today = LocalDate.now(clock)
        dailyMissionSelectionRepository.saveAll(
            listOf(
                DailyMissionSelection(selectedDate = today, mission = generalMissions[0], missionCategory = MissionCategory.GENERAL, displayOrder = 1),
                DailyMissionSelection(selectedDate = today, mission = generalMissions[1], missionCategory = MissionCategory.GENERAL, displayOrder = 2),
                DailyMissionSelection(selectedDate = today, mission = generalMissions[2], missionCategory = MissionCategory.GENERAL, displayOrder = 3),
                DailyMissionSelection(selectedDate = today, mission = specialMission, missionCategory = MissionCategory.SPECIAL, displayOrder = 1),
            ),
        )

        missionCompletionRepository.saveAll(
            listOf(
                completion(userId = requireNotNull(user.id), mission = generalMissions[0], status = MissionCompletionStatus.PENDING, submittedAt = today.atTime(10, 0)),
                completion(userId = requireNotNull(user.id), mission = generalMissions[1], status = MissionCompletionStatus.APPROVED, submittedAt = today.atTime(10, 10)),
                completion(userId = requireNotNull(user.id), mission = specialMission, status = MissionCompletionStatus.REJECTED, submittedAt = today.atTime(10, 20)),
            ),
        )

        clearPersistenceContext()

        repeat(5) {
            missionQueryService.getMissions(requireNotNull(user.id))
            clearPersistenceContext()
        }

        val statistics = entityManagerFactory.unwrap(SessionFactory::class.java).statistics
        statistics.clear()

        val firstRunElapsedMs = measureNanoTime {
            val response = missionQueryService.getMissions(requireNotNull(user.id))
            assertEquals(3, response.generalMissions.size)
            assertEquals("PENDING", response.generalMissions[0].todayStatus?.name)
            assertEquals("APPROVED", response.generalMissions[1].todayStatus?.name)
            assertEquals("REJECTED", response.specialMission?.todayStatus?.name)
        } / 1_000_000.0

        val statementCount = statistics.prepareStatementCount
        clearPersistenceContext()

        val repeatedRunTimesMs = (1..20).map {
            val elapsedNs = measureNanoTime {
                missionQueryService.getMissions(requireNotNull(user.id))
            }
            clearPersistenceContext()
            elapsedNs / 1_000_000.0
        }

        val avgElapsedMs = repeatedRunTimesMs.average()
        val minElapsedMs = repeatedRunTimesMs.minOrNull() ?: 0.0
        val maxElapsedMs = repeatedRunTimesMs.maxOrNull() ?: 0.0

        println(
            "MISSION_QUERY_BASELINE sql=$statementCount firstRunMs=${"%.2f".format(firstRunElapsedMs)} " +
                "avgMs=${"%.2f".format(avgElapsedMs)} minMs=${"%.2f".format(minElapsedMs)} maxMs=${"%.2f".format(maxElapsedMs)}",
        )

        assertTrue(statementCount <= 4)
        assertTrue(avgElapsedMs >= 0.0)
    }

    private fun clearPersistenceContext() {
        entityManager.clear()
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

    private fun completion(
        userId: Long,
        mission: Mission,
        status: MissionCompletionStatus,
        submittedAt: java.time.LocalDateTime,
    ): MissionCompletion = MissionCompletion(
        user = userRepository.findById(userId).orElseThrow(),
        mission = mission,
        photoKey = "missions/$userId/${requireNotNull(mission.id)}/${submittedAt.toLocalDate()}.jpg",
        status = status,
        submittedAt = submittedAt,
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
