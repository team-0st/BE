package com.zerost.api.communitymission.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.communitymission.domain.CommunityMissionCompletion
import com.zerost.api.communitymission.domain.CommunityMissionCompletionRepository
import com.zerost.api.communitymission.domain.CommunityMissionProofRepository
import com.zerost.api.communitymission.domain.CommunityMissionProofRequirementRepository
import com.zerost.api.communitymission.domain.CommunityMissionRepository
import com.zerost.api.communitymission.domain.CommunityMissionProofStatus
import com.zerost.api.communitymission.presentation.dto.CompleteCommunityMissionResponse
import com.zerost.api.user.domain.UserRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime

@Service
class CommunityMissionCompletionService(
    private val userRepository: UserRepository,
    private val communityMissionRepository: CommunityMissionRepository,
    private val communityMissionCompletionRepository: CommunityMissionCompletionRepository,
    private val communityMissionProofRequirementRepository: CommunityMissionProofRequirementRepository,
    private val communityMissionProofRepository: CommunityMissionProofRepository,
    private val communityMissionRewardSettlementService: CommunityMissionRewardSettlementService,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {

    @Transactional
    fun complete(userId: Long, communityMissionId: Long): CompleteCommunityMissionResponse {
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

        val requiredProofCount = communityMissionProofRequirementRepository.findAllByCommunityMissionIdOrderByProofOrderAsc(communityMissionId).size
        if (requiredProofCount == 0) {
            throw BusinessException(ErrorCode.COMMUNITY_MISSION_PROOF_REQUIREMENT_NOT_FOUND)
        }

        val submittedProofCount = communityMissionProofRepository.countByCommunityMissionIdAndUserId(communityMissionId, resolvedUserId)
        if (submittedProofCount != requiredProofCount.toLong()) {
            throw BusinessException(ErrorCode.COMMUNITY_MISSION_PROOFS_INCOMPLETE)
        }

        val approvedProofCount = communityMissionProofRepository.countByCommunityMissionIdAndUserIdAndStatus(
            communityMissionId = communityMissionId,
            userId = resolvedUserId,
            status = CommunityMissionProofStatus.APPROVED,
        )
        if (approvedProofCount != requiredProofCount.toLong()) {
            throw BusinessException(ErrorCode.COMMUNITY_MISSION_PROOFS_NOT_APPROVED)
        }

        val completedAt = LocalDateTime.now()
        val completion = communityMissionCompletionRepository.save(
            CommunityMissionCompletion(
                communityMission = communityMission,
                user = user,
                completedAt = completedAt,
            ),
        )

        val participantCount = communityMissionCompletionRepository.countByCommunityMissionId(communityMissionId)
        val totalUserCount = userRepository.countByOnboardingCompletedTrue()
        val exactAchievementRatio = calculateAchievementRatio(participantCount, totalUserCount, 10)
        val succeededNow = communityMission.hasSucceeded() || exactAchievementRatio >= communityMission.targetRatio

        val currentCompletionReward = if (succeededNow) {
            val rewardResult = communityMissionRewardSettlementService.rewardCurrentCompletion(
                completion = completion,
                user = user,
                rewardedAt = completedAt,
            )

            if (!communityMission.hasSucceeded()) {
                communityMission.markSucceeded(completedAt)
                applicationEventPublisher.publishEvent(
                    CommunityMissionSucceededEvent(
                        communityMissionId = communityMissionId,
                        rewardedAt = completedAt,
                    ),
                )
            }

            rewardResult
        } else {
            null
        }

        return CompleteCommunityMissionResponse(
            completionId = requireNotNull(completion.id),
            communityMissionId = requireNotNull(communityMission.id),
            succeeded = succeededNow,
            rewardGranted = currentCompletionReward != null,
            rewardedEcoJam = currentCompletionReward?.rewardedEcoJam ?: 0,
            rewardedIngredients = currentCompletionReward?.rewardedIngredients ?: emptyList(),
            completedAt = completedAt.toString(),
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
}
