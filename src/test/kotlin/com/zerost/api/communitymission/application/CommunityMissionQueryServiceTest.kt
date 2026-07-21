package com.zerost.api.communitymission.application

import com.zerost.api.communitymission.domain.CommunityMissionCompletionCountProjection
import com.zerost.api.communitymission.domain.CommunityMissionCompletionRepository
import com.zerost.api.communitymission.domain.CommunityMissionRepository
import com.zerost.api.communitymission.domain.CommunityMissionDifficulty
import com.zerost.api.support.createCommunityMission
import com.zerost.api.support.createUser
import com.zerost.api.user.domain.UserRepository
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CommunityMissionQueryServiceTest {

    private val userRepository = mock(UserRepository::class.java)
    private val communityMissionRepository = mock(CommunityMissionRepository::class.java)
    private val communityMissionCompletionRepository = mock(CommunityMissionCompletionRepository::class.java)
    private val communityMissionQueryService = CommunityMissionQueryService(
        userRepository = userRepository,
        communityMissionRepository = communityMissionRepository,
        communityMissionCompletionRepository = communityMissionCompletionRepository,
    )

    @Test
    fun `공동 미션 조회 시 진행률과 성공 여부를 함께 반환한다`() {
        val user = createUser(onboardingCompleted = true)
        val mission1 = createCommunityMission(
            id = 1L,
            difficulty = CommunityMissionDifficulty.ONE_STAR,
            stage = 1,
            targetRatio = java.math.BigDecimal("30.00"),
        )
        val mission2 = createCommunityMission(
            id = 2L,
            difficulty = CommunityMissionDifficulty.ONE_STAR,
            stage = 2,
            targetRatio = java.math.BigDecimal("50.00"),
        )

        `when`(userRepository.findById(1L)).thenReturn(Optional.of(user))
        `when`(userRepository.countByOnboardingCompletedTrue()).thenReturn(20L)
        `when`(communityMissionRepository.findAllByActiveTrue()).thenReturn(listOf(mission2, mission1))
        `when`(communityMissionCompletionRepository.countByCommunityMissionIds(listOf(1L, 2L)))
            .thenReturn(
                listOf(
                    countProjection(1L, 8L),
                    countProjection(2L, 4L),
                ),
            )
        `when`(communityMissionCompletionRepository.findCompletedMissionIdsByUserId(1L)).thenReturn(listOf(1L))

        val response = communityMissionQueryService.getCommunityMissions(1L)

        assertEquals(2, response.size)
        assertEquals(1L, response[0].id)
        assertEquals("40.00", response[0].achievementRatio.toPlainString())
        assertTrue(response[0].succeeded)
        assertTrue(response[0].unlocked)
        assertTrue(response[0].completed)
        assertEquals(2L, response[1].id)
        assertEquals("20.00", response[1].achievementRatio.toPlainString())
        assertFalse(response[1].succeeded)
        assertTrue(response[1].unlocked)
        assertFalse(response[1].completed)
    }

    @Test
    fun `온보딩 완료 유저가 없으면 달성률은 0으로 계산한다`() {
        val user = createUser()
        val mission = createCommunityMission(id = 1L)

        `when`(userRepository.findById(1L)).thenReturn(Optional.of(user))
        `when`(userRepository.countByOnboardingCompletedTrue()).thenReturn(0L)
        `when`(communityMissionRepository.findAllByActiveTrue()).thenReturn(listOf(mission))
        `when`(communityMissionCompletionRepository.countByCommunityMissionIds(listOf(1L))).thenReturn(emptyList())
        `when`(communityMissionCompletionRepository.findCompletedMissionIdsByUserId(1L)).thenReturn(emptyList())

        val response = communityMissionQueryService.getCommunityMissions(1L)

        assertEquals("0.00", response[0].achievementRatio.toPlainString())
        assertFalse(response[0].succeeded)
        assertFalse(response[0].completed)
    }

    @Test
    fun `성공 여부는 반올림 전 달성률 기준으로 판정한다`() {
        val user = createUser()
        val mission = createCommunityMission(
            id = 1L,
            targetRatio = java.math.BigDecimal("16.67"),
        )

        `when`(userRepository.findById(1L)).thenReturn(Optional.of(user))
        `when`(userRepository.countByOnboardingCompletedTrue()).thenReturn(6L)
        `when`(communityMissionRepository.findAllByActiveTrue()).thenReturn(listOf(mission))
        `when`(communityMissionCompletionRepository.countByCommunityMissionIds(listOf(1L)))
            .thenReturn(listOf(countProjection(1L, 1L)))
        `when`(communityMissionCompletionRepository.findCompletedMissionIdsByUserId(1L)).thenReturn(emptyList())

        val response = communityMissionQueryService.getCommunityMissions(1L)

        assertEquals("16.67", response[0].achievementRatio.toPlainString())
        assertFalse(response[0].succeeded)
        assertFalse(response[0].completed)
    }

    @Test
    fun `이전 단계를 완료하지 않으면 다음 단계 공동 미션은 잠금 상태다`() {
        val user = createUser()
        val mission1 = createCommunityMission(
            id = 1L,
            difficulty = CommunityMissionDifficulty.TWO_STAR,
            stage = 1,
        )
        val mission2 = createCommunityMission(
            id = 2L,
            difficulty = CommunityMissionDifficulty.TWO_STAR,
            stage = 2,
        )

        `when`(userRepository.findById(1L)).thenReturn(Optional.of(user))
        `when`(userRepository.countByOnboardingCompletedTrue()).thenReturn(10L)
        `when`(communityMissionRepository.findAllByActiveTrue()).thenReturn(listOf(mission1, mission2))
        `when`(communityMissionCompletionRepository.countByCommunityMissionIds(listOf(1L, 2L))).thenReturn(emptyList())
        `when`(communityMissionCompletionRepository.findCompletedMissionIdsByUserId(1L)).thenReturn(emptyList())

        val response = communityMissionQueryService.getCommunityMissions(1L)

        assertTrue(response[0].unlocked)
        assertFalse(response[1].unlocked)
        assertFalse(response[0].completed)
        assertFalse(response[1].completed)
    }

    private fun countProjection(
        communityMissionId: Long,
        completionCount: Long,
    ): CommunityMissionCompletionCountProjection {
        return object : CommunityMissionCompletionCountProjection {
            override val communityMissionId: Long = communityMissionId
            override val completionCount: Long = completionCount
        }
    }
}
