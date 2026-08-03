package com.zerost.api.communitymission.application

import com.zerost.api.communitymission.domain.CommunityMissionCompletionCountProjection
import com.zerost.api.communitymission.domain.CommunityMissionCompletionRepository
import com.zerost.api.communitymission.domain.CommunityMissionProofCountProjection
import com.zerost.api.communitymission.domain.CommunityMissionProofRepository
import com.zerost.api.communitymission.domain.CommunityMissionProofRequirementRepository
import com.zerost.api.communitymission.domain.CommunityMissionRepository
import com.zerost.api.communitymission.domain.CommunityMissionDifficulty
import com.zerost.api.communitymission.domain.CommunityMissionProofStatus
import com.zerost.api.support.createCommunityMissionProof
import com.zerost.api.support.createCommunityMission
import com.zerost.api.support.createCommunityMissionProofRequirement
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
    private val communityMissionProofRequirementRepository = mock(CommunityMissionProofRequirementRepository::class.java)
    private val communityMissionProofRepository = mock(CommunityMissionProofRepository::class.java)
    private val communityMissionQueryService = CommunityMissionQueryService(
        userRepository = userRepository,
        communityMissionRepository = communityMissionRepository,
        communityMissionCompletionRepository = communityMissionCompletionRepository,
        communityMissionProofRequirementRepository = communityMissionProofRequirementRepository,
        communityMissionProofRepository = communityMissionProofRepository,
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
        `when`(communityMissionProofRequirementRepository.findAllByCommunityMissionIdOrderByProofOrderAsc(1L)).thenReturn(
            listOf(createCommunityMissionProofRequirement(id = 11L, communityMission = mission1)),
        )
        `when`(communityMissionProofRequirementRepository.findAllByCommunityMissionIdOrderByProofOrderAsc(2L)).thenReturn(
            listOf(
                createCommunityMissionProofRequirement(id = 21L, communityMission = mission2, proofOrder = 1),
                createCommunityMissionProofRequirement(id = 22L, communityMission = mission2, proofOrder = 2),
            ),
        )
        `when`(communityMissionProofRepository.countByUserIdAndCommunityMissionIds(1L, listOf(1L, 2L))).thenReturn(
            listOf(
                proofCountProjection(1L, 1L),
                proofCountProjection(2L, 2L),
            ),
        )
        `when`(communityMissionProofRepository.countByUserIdAndCommunityMissionIdsAndStatus(1L, CommunityMissionProofStatus.APPROVED, listOf(1L, 2L))).thenReturn(
            listOf(
                proofCountProjection(1L, 1L),
                proofCountProjection(2L, 1L),
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
        assertEquals(1, response[0].requiredProofCount)
        assertEquals(1, response[0].submittedProofCount)
        assertEquals(1, response[0].approvedProofCount)
        assertEquals(2L, response[1].id)
        assertEquals("20.00", response[1].achievementRatio.toPlainString())
        assertFalse(response[1].succeeded)
        assertTrue(response[1].unlocked)
        assertFalse(response[1].completed)
        assertEquals(2, response[1].requiredProofCount)
        assertEquals(2, response[1].submittedProofCount)
        assertEquals(1, response[1].approvedProofCount)
        assertFalse(response[1].readyToComplete)
    }

    @Test
    fun `온보딩 완료 유저가 없으면 달성률은 0으로 계산한다`() {
        val user = createUser()
        val mission = createCommunityMission(id = 1L)

        `when`(userRepository.findById(1L)).thenReturn(Optional.of(user))
        `when`(userRepository.countByOnboardingCompletedTrue()).thenReturn(0L)
        `when`(communityMissionRepository.findAllByActiveTrue()).thenReturn(listOf(mission))
        `when`(communityMissionCompletionRepository.countByCommunityMissionIds(listOf(1L))).thenReturn(emptyList())
        `when`(communityMissionProofRequirementRepository.findAllByCommunityMissionIdOrderByProofOrderAsc(1L)).thenReturn(emptyList())
        `when`(communityMissionProofRepository.countByUserIdAndCommunityMissionIds(1L, listOf(1L))).thenReturn(emptyList())
        `when`(communityMissionProofRepository.countByUserIdAndCommunityMissionIdsAndStatus(1L, CommunityMissionProofStatus.APPROVED, listOf(1L))).thenReturn(emptyList())
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
        `when`(communityMissionProofRequirementRepository.findAllByCommunityMissionIdOrderByProofOrderAsc(1L)).thenReturn(emptyList())
        `when`(communityMissionProofRepository.countByUserIdAndCommunityMissionIds(1L, listOf(1L))).thenReturn(emptyList())
        `when`(communityMissionProofRepository.countByUserIdAndCommunityMissionIdsAndStatus(1L, CommunityMissionProofStatus.APPROVED, listOf(1L))).thenReturn(emptyList())
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
        `when`(communityMissionProofRequirementRepository.findAllByCommunityMissionIdOrderByProofOrderAsc(1L)).thenReturn(emptyList())
        `when`(communityMissionProofRequirementRepository.findAllByCommunityMissionIdOrderByProofOrderAsc(2L)).thenReturn(emptyList())
        `when`(communityMissionProofRepository.countByUserIdAndCommunityMissionIds(1L, listOf(1L, 2L))).thenReturn(emptyList())
        `when`(communityMissionProofRepository.countByUserIdAndCommunityMissionIdsAndStatus(1L, CommunityMissionProofStatus.APPROVED, listOf(1L, 2L))).thenReturn(emptyList())
        `when`(communityMissionCompletionRepository.findCompletedMissionIdsByUserId(1L)).thenReturn(emptyList())

        val response = communityMissionQueryService.getCommunityMissions(1L)

        assertTrue(response[0].unlocked)
        assertFalse(response[1].unlocked)
        assertFalse(response[0].completed)
        assertFalse(response[1].completed)
    }

    @Test
    fun `공동 미션 상세 조회 시 검수 상태를 함께 반환한다`() {
        val user = createUser(id = 1L, onboardingCompleted = true)
        val mission = createCommunityMission(id = 3L)
        val requirement = createCommunityMissionProofRequirement(
            id = 11L,
            communityMission = mission,
            proofOrder = 1,
        )
        val approvedProof = createCommunityMissionProof(
            id = 101L,
            communityMission = mission,
            proofRequirement = requirement,
            user = user,
            status = CommunityMissionProofStatus.APPROVED,
        )

        `when`(userRepository.findById(1L)).thenReturn(Optional.of(user))
        `when`(communityMissionRepository.findAllByActiveTrue()).thenReturn(listOf(mission))
        `when`(communityMissionCompletionRepository.findCompletedMissionIdsByUserId(1L)).thenReturn(emptyList())
        `when`(communityMissionProofRequirementRepository.findAllByCommunityMissionIdOrderByProofOrderAsc(3L)).thenReturn(listOf(requirement))
        `when`(communityMissionProofRepository.findAllByCommunityMissionIdAndUserIdOrderByProofRequirementProofOrderAsc(3L, 1L))
            .thenReturn(listOf(approvedProof))

        val response = communityMissionQueryService.getCommunityMission(1L, 3L)

        assertEquals(1, response.approvedProofCount)
        assertTrue(response.readyToComplete)
        assertEquals("APPROVED", response.proofRequirements[0].reviewStatus)
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

    private fun proofCountProjection(
        communityMissionId: Long,
        proofCount: Long,
    ): CommunityMissionProofCountProjection {
        return object : CommunityMissionProofCountProjection {
            override val communityMissionId: Long = communityMissionId
            override val proofCount: Long = proofCount
        }
    }
}
