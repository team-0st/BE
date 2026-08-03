package com.zerost.api.communitymission.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.communitymission.domain.CommunityMissionProof
import com.zerost.api.communitymission.domain.CommunityMissionProofRepository
import com.zerost.api.communitymission.domain.CommunityMissionProofRequirementRepository
import com.zerost.api.communitymission.domain.CommunityMissionCompletionRepository
import com.zerost.api.communitymission.domain.CommunityMissionRepository
import com.zerost.api.communitymission.domain.CommunityMissionProofStatus
import com.zerost.api.file.application.FileUploadService
import com.zerost.api.support.createCommunityMission
import com.zerost.api.support.createCommunityMissionProof
import com.zerost.api.support.createCommunityMissionProofRequirement
import com.zerost.api.support.createUser
import com.zerost.api.user.domain.UserRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.never
import org.mockito.Mockito.`when`
import java.util.Optional
import kotlin.test.assertEquals

class CommunityMissionProofServiceTest {

    private val userRepository = mock(UserRepository::class.java)
    private val communityMissionRepository = mock(CommunityMissionRepository::class.java)
    private val communityMissionCompletionRepository = mock(CommunityMissionCompletionRepository::class.java)
    private val communityMissionProofRequirementRepository = mock(CommunityMissionProofRequirementRepository::class.java)
    private val communityMissionProofRepository = mock(CommunityMissionProofRepository::class.java)
    private val fileUploadService = mock(FileUploadService::class.java)
    private val communityMissionProofService = CommunityMissionProofService(
        userRepository = userRepository,
        communityMissionRepository = communityMissionRepository,
        communityMissionCompletionRepository = communityMissionCompletionRepository,
        communityMissionProofRequirementRepository = communityMissionProofRequirementRepository,
        communityMissionProofRepository = communityMissionProofRepository,
        fileUploadService = fileUploadService,
    )

    @Test
    fun `공동 미션 인증 단계를 제출할 수 있다`() {
        val user = createUser(id = 1L, onboardingCompleted = true)
        val mission = createCommunityMission(id = 3L)
        val requirement = createCommunityMissionProofRequirement(
            id = 11L,
            communityMission = mission,
            proofOrder = 1,
            requiredImageCount = 2,
        )

        `when`(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user))
        `when`(communityMissionRepository.findAllByActiveTrue()).thenReturn(listOf(mission))
        `when`(communityMissionRepository.findByIdAndActiveTrueForUpdate(3L)).thenReturn(mission)
        `when`(communityMissionCompletionRepository.findCompletedMissionIdsByUserId(1L)).thenReturn(emptyList())
        `when`(communityMissionCompletionRepository.existsByCommunityMissionIdAndUserId(3L, 1L)).thenReturn(false)
        `when`(communityMissionProofRequirementRepository.findByIdAndCommunityMissionId(11L, 3L)).thenReturn(requirement)
        `when`(communityMissionProofRepository.findByProofRequirementIdAndUserId(11L, 1L)).thenReturn(null)
        `when`(communityMissionProofRepository.save(any(CommunityMissionProof::class.java))).thenAnswer { invocation ->
            val proof = invocation.arguments[0] as CommunityMissionProof
            CommunityMissionProof(
                id = 101L,
                communityMission = proof.communityMission,
                proofRequirement = proof.proofRequirement,
                user = proof.user,
                status = proof.status,
                submittedAt = proof.submittedAt,
                reviewedAt = proof.reviewedAt,
                images = proof.images,
            )
        }
        `when`(communityMissionProofRequirementRepository.findAllByCommunityMissionIdOrderByProofOrderAsc(3L)).thenReturn(listOf(requirement))
        `when`(communityMissionProofRepository.countByCommunityMissionIdAndUserIdAndStatus(3L, 1L, CommunityMissionProofStatus.APPROVED)).thenReturn(1L)

        val response = communityMissionProofService.submitProof(
            userId = 1L,
            communityMissionId = 3L,
            requirementId = 11L,
            photoKeys = listOf(
                "community-missions/1/3/2026/07/21/proof-1.jpg",
                "community-missions/1/3/2026/07/21/proof-2.jpg",
            ),
        )

        assertEquals(101L, response.proofId)
        assertEquals(3L, response.communityMissionId)
        assertEquals(11L, response.requirementId)
        assertEquals(true, response.readyToComplete)
        verify(fileUploadService).validateCommunityMissionImageKey(1L, 3L, "community-missions/1/3/2026/07/21/proof-1.jpg")
        verify(fileUploadService).validateCommunityMissionImageKey(1L, 3L, "community-missions/1/3/2026/07/21/proof-2.jpg")
    }

    @Test
    fun `필수 이미지 수와 다르면 제출할 수 없다`() {
        val user = createUser(id = 1L, onboardingCompleted = true)
        val mission = createCommunityMission(id = 3L)
        val requirement = createCommunityMissionProofRequirement(
            id = 11L,
            communityMission = mission,
            requiredImageCount = 2,
        )

        `when`(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user))
        `when`(communityMissionRepository.findAllByActiveTrue()).thenReturn(listOf(mission))
        `when`(communityMissionRepository.findByIdAndActiveTrueForUpdate(3L)).thenReturn(mission)
        `when`(communityMissionCompletionRepository.findCompletedMissionIdsByUserId(1L)).thenReturn(emptyList())
        `when`(communityMissionCompletionRepository.existsByCommunityMissionIdAndUserId(3L, 1L)).thenReturn(false)
        `when`(communityMissionProofRequirementRepository.findByIdAndCommunityMissionId(11L, 3L)).thenReturn(requirement)
        `when`(communityMissionProofRepository.findByProofRequirementIdAndUserId(11L, 1L)).thenReturn(null)

        val exception = assertThrows<BusinessException> {
            communityMissionProofService.submitProof(
                userId = 1L,
                communityMissionId = 3L,
                requirementId = 11L,
                photoKeys = listOf("community-missions/1/3/2026/07/21/proof-1.jpg"),
            )
        }

        assertEquals(ErrorCode.COMMUNITY_MISSION_INVALID_PROOF_IMAGE_COUNT, exception.errorCode)
        verify(fileUploadService, never()).validateCommunityMissionImageKey(1L, 3L, "community-missions/1/3/2026/07/21/proof-1.jpg")
    }

    @Test
    fun `반려된 공동 미션 인증은 다시 제출할 수 있다`() {
        val user = createUser(id = 1L, onboardingCompleted = true)
        val mission = createCommunityMission(id = 3L)
        val requirement = createCommunityMissionProofRequirement(
            id = 11L,
            communityMission = mission,
            proofOrder = 1,
            requiredImageCount = 1,
        )
        val rejectedProof = createCommunityMissionProof(
            id = 101L,
            communityMission = mission,
            proofRequirement = requirement,
            user = user,
            status = CommunityMissionProofStatus.REJECTED,
            imageKeys = listOf("community-missions/1/3/2026/07/21/old-proof.jpg"),
        )

        `when`(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user))
        `when`(communityMissionRepository.findAllByActiveTrue()).thenReturn(listOf(mission))
        `when`(communityMissionRepository.findByIdAndActiveTrueForUpdate(3L)).thenReturn(mission)
        `when`(communityMissionCompletionRepository.findCompletedMissionIdsByUserId(1L)).thenReturn(emptyList())
        `when`(communityMissionCompletionRepository.existsByCommunityMissionIdAndUserId(3L, 1L)).thenReturn(false)
        `when`(communityMissionProofRequirementRepository.findByIdAndCommunityMissionId(11L, 3L)).thenReturn(requirement)
        `when`(communityMissionProofRepository.findByProofRequirementIdAndUserId(11L, 1L)).thenReturn(rejectedProof)
        `when`(communityMissionProofRequirementRepository.findAllByCommunityMissionIdOrderByProofOrderAsc(3L)).thenReturn(listOf(requirement))
        `when`(communityMissionProofRepository.countByCommunityMissionIdAndUserIdAndStatus(3L, 1L, CommunityMissionProofStatus.APPROVED)).thenReturn(0L)

        val response = communityMissionProofService.submitProof(
            userId = 1L,
            communityMissionId = 3L,
            requirementId = 11L,
            photoKeys = listOf("community-missions/1/3/2026/07/21/new-proof.jpg"),
        )

        assertEquals(101L, response.proofId)
        assertEquals("community-missions/1/3/2026/07/21/new-proof.jpg", rejectedProof.images.single().imageKey)
        assertEquals(CommunityMissionProofStatus.PENDING, rejectedProof.status)
        assertEquals(false, response.readyToComplete)
    }
}
