package com.zerost.api.communitymission.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.communitymission.domain.CommunityMission
import com.zerost.api.communitymission.domain.CommunityMissionCompletionRepository
import com.zerost.api.communitymission.domain.CommunityMissionProofRepository
import com.zerost.api.communitymission.domain.CommunityMissionProofRequirementRepository
import com.zerost.api.communitymission.presentation.dto.CommunityMissionDetailResponse
import com.zerost.api.communitymission.presentation.dto.CommunityMissionProofRequirementResponse
import com.zerost.api.communitymission.domain.CommunityMissionRepository
import com.zerost.api.communitymission.presentation.dto.CommunityMissionProgressResponse
import com.zerost.api.user.domain.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class CommunityMissionQueryService(
    private val userRepository: UserRepository,
    private val communityMissionRepository: CommunityMissionRepository,
    private val communityMissionCompletionRepository: CommunityMissionCompletionRepository,
    private val communityMissionProofRequirementRepository: CommunityMissionProofRequirementRepository,
    private val communityMissionProofRepository: CommunityMissionProofRepository,
) {

    @Transactional(readOnly = true)
    fun getCommunityMissions(userId: Long): List<CommunityMissionProgressResponse> {
        val user = userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }
        val resolvedUserId = requireNotNull(user.id)

        val communityMissions = CommunityMissionUnlockPolicy.sort(communityMissionRepository.findAllByActiveTrue())

        if (communityMissions.isEmpty()) {
            return emptyList()
        }

        val missionIds = communityMissions.map { requireNotNull(it.id) }
        val completionCounts = communityMissionCompletionRepository.countByCommunityMissionIds(missionIds)
            .associate { it.communityMissionId to it.completionCount }
        val proofRequirementCounts = missionIds.associateWith { communityMissionProofRequirementRepository.findAllByCommunityMissionIdOrderByProofOrderAsc(it).size }
        val proofCounts = communityMissionProofRepository.countByUserIdAndCommunityMissionIds(resolvedUserId, missionIds)
            .associate { it.communityMissionId to it.proofCount.toInt() }
        val completedMissionIds = communityMissionCompletionRepository.findCompletedMissionIdsByUserId(resolvedUserId).toSet()
        val totalUserCount = userRepository.countByOnboardingCompletedTrue()

        return communityMissions.map { communityMission ->
            val missionId = requireNotNull(communityMission.id)
            val participantCount = completionCounts[missionId] ?: 0L
            val requiredProofCount = proofRequirementCounts[missionId] ?: 0
            val submittedProofCount = proofCounts[missionId] ?: 0
            val exactAchievementRatio = calculateAchievementRatio(
                participantCount = participantCount,
                totalUserCount = totalUserCount,
                scale = 10,
            )
            val achievementRatio = calculateAchievementRatio(
                participantCount = participantCount,
                totalUserCount = totalUserCount,
                scale = 2,
            )

            CommunityMissionProgressResponse(
                id = missionId,
                title = communityMission.title,
                description = communityMission.description,
                imageUrl = communityMission.imageUrl,
                difficulty = communityMission.difficulty.name,
                stage = communityMission.stage,
                targetRatio = communityMission.targetRatio,
                achievementRatio = achievementRatio,
                participantCount = participantCount,
                totalUserCount = totalUserCount,
                succeeded = communityMission.hasSucceeded() || isSucceeded(exactAchievementRatio, communityMission.targetRatio),
                unlocked = CommunityMissionUnlockPolicy.isUnlocked(communityMissions, communityMission, completedMissionIds),
                completed = missionId in completedMissionIds,
                requiredProofCount = requiredProofCount,
                submittedProofCount = submittedProofCount,
                readyToComplete = requiredProofCount > 0 && submittedProofCount == requiredProofCount && missionId !in completedMissionIds,
            )
        }
    }

    @Transactional(readOnly = true)
    fun getCommunityMission(userId: Long, communityMissionId: Long): CommunityMissionDetailResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }
        val resolvedUserId = requireNotNull(user.id)
        val communityMissions = CommunityMissionUnlockPolicy.sort(communityMissionRepository.findAllByActiveTrue())
        val communityMission = communityMissions.firstOrNull { it.id == communityMissionId }
            ?: throw BusinessException(ErrorCode.COMMUNITY_MISSION_NOT_FOUND)
        val completedMissionIds = communityMissionCompletionRepository.findCompletedMissionIdsByUserId(resolvedUserId).toSet()
        val proofRequirements = communityMissionProofRequirementRepository.findAllByCommunityMissionIdOrderByProofOrderAsc(communityMissionId)
        val submittedProofs = communityMissionProofRepository
            .findAllByCommunityMissionIdAndUserIdOrderByProofRequirementProofOrderAsc(communityMissionId, resolvedUserId)
            .associateBy { requireNotNull(it.proofRequirement.id) }

        return CommunityMissionDetailResponse(
            id = requireNotNull(communityMission.id),
            title = communityMission.title,
            description = communityMission.description,
            imageUrl = communityMission.imageUrl,
            difficulty = communityMission.difficulty.name,
            stage = communityMission.stage,
            targetRatio = communityMission.targetRatio,
            succeeded = communityMission.hasSucceeded(),
            unlocked = CommunityMissionUnlockPolicy.isUnlocked(communityMissions, communityMission, completedMissionIds),
            completed = communityMissionId in completedMissionIds,
            requiredProofCount = proofRequirements.size,
            submittedProofCount = submittedProofs.size,
            readyToComplete = proofRequirements.isNotEmpty() && proofRequirements.size == submittedProofs.size && communityMissionId !in completedMissionIds,
            proofRequirements = proofRequirements.map { requirement ->
                val submittedProof = submittedProofs[requireNotNull(requirement.id)]
                CommunityMissionProofRequirementResponse(
                    requirementId = requireNotNull(requirement.id),
                    proofOrder = requirement.proofOrder,
                    title = requirement.title,
                    description = requirement.description,
                    requiredImageCount = requirement.requiredImageCount,
                    requiredDayOffset = requirement.requiredDayOffset,
                    submitted = submittedProof != null,
                    submittedProofId = submittedProof?.id,
                    submittedAt = submittedProof?.submittedAt?.toString(),
                    submittedImageKeys = submittedProof?.images?.sortedBy { it.imageOrder }?.map { it.imageKey } ?: emptyList(),
                )
            },
        )
    }

    private fun calculateAchievementRatio(
        participantCount: Long,
        totalUserCount: Long,
        scale: Int,
    ): BigDecimal {
        if (totalUserCount == 0L) {
            return BigDecimal.ZERO.setScale(scale)
        }

        return BigDecimal.valueOf(participantCount)
            .multiply(BigDecimal("100"))
            .divide(BigDecimal.valueOf(totalUserCount), scale, RoundingMode.HALF_UP)
    }

    private fun isSucceeded(
        achievementRatio: BigDecimal,
        targetRatio: BigDecimal,
    ): Boolean {
        return achievementRatio >= targetRatio
    }
}
