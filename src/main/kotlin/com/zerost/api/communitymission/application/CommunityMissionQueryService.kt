package com.zerost.api.communitymission.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.communitymission.domain.CommunityMission
import com.zerost.api.communitymission.domain.CommunityMissionCompletionRepository
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
        val completedMissionIds = communityMissionCompletionRepository.findCompletedMissionIdsByUserId(resolvedUserId).toSet()
        val totalUserCount = userRepository.countByOnboardingCompletedTrue()

        return communityMissions.map { communityMission ->
            val missionId = requireNotNull(communityMission.id)
            val participantCount = completionCounts[missionId] ?: 0L
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
            )
        }
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
