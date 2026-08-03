package com.zerost.api.communitymission.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.communitymission.domain.CommunityMissionCompletion
import com.zerost.api.communitymission.domain.CommunityMissionDifficulty
import com.zerost.api.communitymission.domain.CommunityMissionCompletionRepository
import com.zerost.api.communitymission.domain.CommunityMissionProofStatus
import com.zerost.api.communitymission.domain.CommunityMissionProofRepository
import com.zerost.api.communitymission.domain.CommunityMissionProofRequirementRepository
import com.zerost.api.communitymission.domain.CommunityMissionRepository
import com.zerost.api.support.createCommunityMissionCompletion
import com.zerost.api.support.createCommunityMission
import com.zerost.api.support.createCommunityMissionProofRequirement
import com.zerost.api.support.createUser
import com.zerost.api.user.domain.UserRepository
import org.springframework.context.ApplicationEventPublisher
import org.junit.jupiter.api.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Suppress("UNCHECKED_CAST")
private fun <T> anyObject(): T {
    any<T>()
    return null as T
}

class CommunityMissionCompletionServiceTest {

    private val userRepository = mock(UserRepository::class.java)
    private val communityMissionRepository = mock(CommunityMissionRepository::class.java)
    private val communityMissionCompletionRepository = mock(CommunityMissionCompletionRepository::class.java)
    private val communityMissionProofRequirementRepository = mock(CommunityMissionProofRequirementRepository::class.java)
    private val communityMissionProofRepository = mock(CommunityMissionProofRepository::class.java)
    private val communityMissionRewardSettlementService = mock(CommunityMissionRewardSettlementService::class.java)
    private val applicationEventPublisher = mock(ApplicationEventPublisher::class.java)
    private val communityMissionCompletionService = CommunityMissionCompletionService(
        userRepository = userRepository,
        communityMissionRepository = communityMissionRepository,
        communityMissionCompletionRepository = communityMissionCompletionRepository,
        communityMissionProofRequirementRepository = communityMissionProofRequirementRepository,
        communityMissionProofRepository = communityMissionProofRepository,
        communityMissionRewardSettlementService = communityMissionRewardSettlementService,
        applicationEventPublisher = applicationEventPublisher,
    )

    @Test
    fun `달성률이 목표 미만이면 완료만 처리하고 보상은 지급하지 않는다`() {
        val user = createUser(onboardingCompleted = true)
        val mission1 = createCommunityMission(
            id = 1L,
            difficulty = CommunityMissionDifficulty.ONE_STAR,
            stage = 1,
        )
        val mission2 = createCommunityMission(
            id = 2L,
            difficulty = CommunityMissionDifficulty.ONE_STAR,
            stage = 2,
            targetRatio = java.math.BigDecimal("50.00"),
        )

        `when`(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user))
        `when`(communityMissionRepository.findAllByActiveTrue()).thenReturn(listOf(mission2, mission1))
        `when`(communityMissionRepository.findByIdAndActiveTrueForUpdate(2L)).thenReturn(mission2)
        `when`(communityMissionCompletionRepository.findCompletedMissionIdsByUserId(1L)).thenReturn(listOf(1L))
        `when`(communityMissionCompletionRepository.existsByCommunityMissionIdAndUserId(2L, 1L)).thenReturn(false)
        `when`(communityMissionProofRequirementRepository.findAllByCommunityMissionIdOrderByProofOrderAsc(2L)).thenReturn(
            listOf(createCommunityMissionProofRequirement(id = 21L, communityMission = mission2)),
        )
        `when`(communityMissionProofRepository.countByCommunityMissionIdAndUserId(2L, 1L)).thenReturn(1L)
        `when`(communityMissionProofRepository.countByCommunityMissionIdAndUserIdAndStatus(2L, 1L, CommunityMissionProofStatus.APPROVED)).thenReturn(1L)
        `when`(communityMissionCompletionRepository.save(any(CommunityMissionCompletion::class.java))).thenReturn(
            createCommunityMissionCompletion(id = 10L, communityMission = mission2, user = user),
        )
        `when`(communityMissionCompletionRepository.countByCommunityMissionId(2L)).thenReturn(4L)
        `when`(userRepository.countByOnboardingCompletedTrue()).thenReturn(10L)

        val response = communityMissionCompletionService.complete(1L, 2L)

        assertEquals(10L, response.completionId)
        assertEquals(2L, response.communityMissionId)
        assertEquals(false, response.succeeded)
        assertEquals(false, response.rewardGranted)
        assertEquals(0, response.rewardedEcoJam)
        assertEquals(0, response.rewardedIngredients.size)
        assertEquals(0, user.ecoJam)
        verifyNoInteractions(communityMissionRewardSettlementService, applicationEventPublisher)
    }

    @Test
    fun `온보딩을 완료하지 않으면 공동 미션을 완료할 수 없다`() {
        val user = createUser(onboardingCompleted = false)

        `when`(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user))

        val exception = assertFailsWith<BusinessException> {
            communityMissionCompletionService.complete(1L, 1L)
        }

        assertEquals(ErrorCode.COMMUNITY_MISSION_ONBOARDING_REQUIRED, exception.errorCode)
        verify(communityMissionRepository, never()).findAllByActiveTrue()
    }

    @Test
    fun `이전 단계를 완료하지 않으면 잠긴 공동 미션을 완료할 수 없다`() {
        val user = createUser(onboardingCompleted = true)
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

        `when`(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user))
        `when`(communityMissionRepository.findAllByActiveTrue()).thenReturn(listOf(mission1, mission2))
        `when`(communityMissionRepository.findByIdAndActiveTrueForUpdate(2L)).thenReturn(mission2)
        `when`(communityMissionCompletionRepository.findCompletedMissionIdsByUserId(1L)).thenReturn(emptyList())

        val exception = assertFailsWith<BusinessException> {
            communityMissionCompletionService.complete(1L, 2L)
        }

        assertEquals(ErrorCode.COMMUNITY_MISSION_NOT_UNLOCKED, exception.errorCode)
    }

    @Test
    fun `이미 완료한 공동 미션은 다시 완료할 수 없다`() {
        val user = createUser(onboardingCompleted = true)
        val mission = createCommunityMission(id = 1L)

        `when`(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user))
        `when`(communityMissionRepository.findAllByActiveTrue()).thenReturn(listOf(mission))
        `when`(communityMissionRepository.findByIdAndActiveTrueForUpdate(1L)).thenReturn(mission)
        `when`(communityMissionCompletionRepository.findCompletedMissionIdsByUserId(1L)).thenReturn(listOf(1L))
        `when`(communityMissionCompletionRepository.existsByCommunityMissionIdAndUserId(1L, 1L)).thenReturn(true)

        val exception = assertFailsWith<BusinessException> {
            communityMissionCompletionService.complete(1L, 1L)
        }

        assertEquals(ErrorCode.COMMUNITY_MISSION_ALREADY_COMPLETED, exception.errorCode)
    }

    @Test
    fun `목표 달성 시 현재 완료자는 즉시 보상하고 성공 이벤트를 발행한다`() {
        val user = createUser(id = 1L, onboardingCompleted = true)
        val mission = createCommunityMission(
            id = 1L,
            targetRatio = java.math.BigDecimal("50.00"),
        )
        val currentCompletion = createCommunityMissionCompletion(id = 101L, communityMission = mission, user = user)
        val rewardResult = CommunityMissionRewardSettlementService.CompletionRewardResult(
            rewardedEcoJam = 50,
            rewardedIngredients = emptyList(),
        )

        `when`(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user))
        `when`(communityMissionRepository.findAllByActiveTrue()).thenReturn(listOf(mission))
        `when`(communityMissionRepository.findByIdAndActiveTrueForUpdate(1L)).thenReturn(mission)
        `when`(communityMissionCompletionRepository.findCompletedMissionIdsByUserId(1L)).thenReturn(emptyList())
        `when`(communityMissionCompletionRepository.existsByCommunityMissionIdAndUserId(1L, 1L)).thenReturn(false)
        `when`(communityMissionProofRequirementRepository.findAllByCommunityMissionIdOrderByProofOrderAsc(1L)).thenReturn(
            listOf(createCommunityMissionProofRequirement(id = 11L, communityMission = mission)),
        )
        `when`(communityMissionProofRepository.countByCommunityMissionIdAndUserId(1L, 1L)).thenReturn(1L)
        `when`(communityMissionProofRepository.countByCommunityMissionIdAndUserIdAndStatus(1L, 1L, CommunityMissionProofStatus.APPROVED)).thenReturn(1L)
        `when`(communityMissionCompletionRepository.save(any(CommunityMissionCompletion::class.java))).thenReturn(currentCompletion)
        `when`(communityMissionCompletionRepository.countByCommunityMissionId(1L)).thenReturn(5L)
        `when`(userRepository.countByOnboardingCompletedTrue()).thenReturn(10L)
        `when`(
            communityMissionRewardSettlementService.rewardCurrentCompletion(
                anyObject(),
                anyObject(),
                anyObject(),
            ),
        ).thenAnswer {
            currentCompletion.markRewarded(currentCompletion.completedAt)
            rewardResult
        }

        val response = communityMissionCompletionService.complete(1L, 1L)

        assertEquals(true, response.succeeded)
        assertEquals(true, response.rewardGranted)
        assertEquals(50, response.rewardedEcoJam)
        assertEquals(true, mission.hasSucceeded())
        assertEquals(true, currentCompletion.isRewarded())
    }

    @Test
    fun `필수 인증 단계를 모두 제출하지 않으면 완료할 수 없다`() {
        val user = createUser(id = 1L, onboardingCompleted = true)
        val mission = createCommunityMission(id = 1L)

        `when`(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user))
        `when`(communityMissionRepository.findAllByActiveTrue()).thenReturn(listOf(mission))
        `when`(communityMissionRepository.findByIdAndActiveTrueForUpdate(1L)).thenReturn(mission)
        `when`(communityMissionCompletionRepository.findCompletedMissionIdsByUserId(1L)).thenReturn(emptyList())
        `when`(communityMissionCompletionRepository.existsByCommunityMissionIdAndUserId(1L, 1L)).thenReturn(false)
        `when`(communityMissionProofRequirementRepository.findAllByCommunityMissionIdOrderByProofOrderAsc(1L)).thenReturn(
            listOf(
                createCommunityMissionProofRequirement(id = 11L, communityMission = mission, proofOrder = 1),
                createCommunityMissionProofRequirement(id = 12L, communityMission = mission, proofOrder = 2),
            ),
        )
        `when`(communityMissionProofRepository.countByCommunityMissionIdAndUserId(1L, 1L)).thenReturn(1L)

        val exception = assertFailsWith<BusinessException> {
            communityMissionCompletionService.complete(1L, 1L)
        }

        assertEquals(ErrorCode.COMMUNITY_MISSION_PROOFS_INCOMPLETE, exception.errorCode)
    }

    @Test
    fun `필수 인증 단계가 모두 승인되지 않으면 완료할 수 없다`() {
        val user = createUser(id = 1L, onboardingCompleted = true)
        val mission = createCommunityMission(id = 1L)

        `when`(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user))
        `when`(communityMissionRepository.findAllByActiveTrue()).thenReturn(listOf(mission))
        `when`(communityMissionRepository.findByIdAndActiveTrueForUpdate(1L)).thenReturn(mission)
        `when`(communityMissionCompletionRepository.findCompletedMissionIdsByUserId(1L)).thenReturn(emptyList())
        `when`(communityMissionCompletionRepository.existsByCommunityMissionIdAndUserId(1L, 1L)).thenReturn(false)
        `when`(communityMissionProofRequirementRepository.findAllByCommunityMissionIdOrderByProofOrderAsc(1L)).thenReturn(
            listOf(createCommunityMissionProofRequirement(id = 11L, communityMission = mission)),
        )
        `when`(communityMissionProofRepository.countByCommunityMissionIdAndUserId(1L, 1L)).thenReturn(1L)
        `when`(communityMissionProofRepository.countByCommunityMissionIdAndUserIdAndStatus(1L, 1L, CommunityMissionProofStatus.APPROVED)).thenReturn(0L)

        val exception = assertFailsWith<BusinessException> {
            communityMissionCompletionService.complete(1L, 1L)
        }

        assertEquals(ErrorCode.COMMUNITY_MISSION_PROOFS_NOT_APPROVED, exception.errorCode)
    }
}
