package com.zerost.api.communitymission.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.communitymission.domain.CommunityMission
import com.zerost.api.communitymission.domain.CommunityMissionCompletionRepository
import com.zerost.api.communitymission.domain.CommunityMissionProof
import com.zerost.api.communitymission.domain.CommunityMissionProofRepository
import com.zerost.api.communitymission.domain.CommunityMissionProofRequirementRepository
import com.zerost.api.communitymission.domain.CommunityMissionRepository
import com.zerost.api.communitymission.domain.CommunityMissionProofStatus
import com.zerost.api.communitymission.presentation.dto.SubmitCommunityMissionProofResponse
import com.zerost.api.file.application.FileUploadService
import com.zerost.api.user.domain.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class CommunityMissionProofService(
    private val userRepository: UserRepository,
    private val communityMissionRepository: CommunityMissionRepository,
    private val communityMissionCompletionRepository: CommunityMissionCompletionRepository,
    private val communityMissionProofRequirementRepository: CommunityMissionProofRequirementRepository,
    private val communityMissionProofRepository: CommunityMissionProofRepository,
    private val fileUploadService: FileUploadService,
) {

    @Transactional
    fun submitProof(
        userId: Long,
        communityMissionId: Long,
        requirementId: Long,
        photoKeys: List<String>,
    ): SubmitCommunityMissionProofResponse {
        val user = userRepository.findByIdForUpdate(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        if (!user.onboardingCompleted) {
            throw BusinessException(ErrorCode.COMMUNITY_MISSION_ONBOARDING_REQUIRED)
        }

        val communityMissions = CommunityMissionUnlockPolicy.sort(communityMissionRepository.findAllByActiveTrue())
        val communityMission = communityMissionRepository.findByIdAndActiveTrueForUpdate(communityMissionId)
            ?: throw BusinessException(ErrorCode.COMMUNITY_MISSION_NOT_FOUND)
        val resolvedUserId = requireNotNull(user.id)
        val completedMissionIds = communityMissionCompletionRepository.findCompletedMissionIdsByUserId(resolvedUserId).toSet()

        if (!CommunityMissionUnlockPolicy.isUnlocked(communityMissions, communityMission, completedMissionIds)) {
            throw BusinessException(ErrorCode.COMMUNITY_MISSION_NOT_UNLOCKED)
        }

        if (communityMissionCompletionRepository.existsByCommunityMissionIdAndUserId(communityMissionId, resolvedUserId)) {
            throw BusinessException(ErrorCode.COMMUNITY_MISSION_ALREADY_COMPLETED)
        }

        val requirement = communityMissionProofRequirementRepository.findByIdAndCommunityMissionId(requirementId, communityMissionId)
            ?: throw BusinessException(ErrorCode.COMMUNITY_MISSION_PROOF_REQUIREMENT_NOT_FOUND)

        if (photoKeys.size != requirement.requiredImageCount) {
            throw BusinessException(ErrorCode.COMMUNITY_MISSION_INVALID_PROOF_IMAGE_COUNT)
        }

        photoKeys.forEach { photoKey ->
            fileUploadService.validateCommunityMissionImageKey(
                userId = resolvedUserId,
                communityMissionId = communityMissionId,
                fileKey = photoKey,
            )
        }

        val submittedAt = LocalDateTime.now()
        val existingProof = communityMissionProofRepository.findByProofRequirementIdAndUserId(requirementId, resolvedUserId)
        val savedProof = if (existingProof == null) {
            val proof = CommunityMissionProof.submit(
                communityMission = communityMission,
                proofRequirement = requirement,
                user = user,
                submittedAt = submittedAt,
            ).apply {
                photoKeys.forEachIndexed { index, photoKey ->
                    addImage(
                        imageKey = photoKey,
                        imageOrder = index + 1,
                    )
                }
            }
            communityMissionProofRepository.save(proof)
        } else {
            existingProof.resubmit(
                submittedAt = submittedAt,
                imageKeys = photoKeys,
            )
            existingProof
        }
        val requiredProofCount = communityMissionProofRequirementRepository.findAllByCommunityMissionIdOrderByProofOrderAsc(communityMissionId).size
        val approvedProofCount = communityMissionProofRepository.countByCommunityMissionIdAndUserIdAndStatus(
            communityMissionId = communityMissionId,
            userId = resolvedUserId,
            status = CommunityMissionProofStatus.APPROVED,
        )

        return SubmitCommunityMissionProofResponse(
            proofId = requireNotNull(savedProof.id),
            communityMissionId = communityMissionId,
            requirementId = requireNotNull(requirement.id),
            proofOrder = requirement.proofOrder,
            submittedAt = submittedAt.toString(),
            readyToComplete = approvedProofCount == requiredProofCount.toLong(),
        )
    }
}
