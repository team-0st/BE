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
    fun getCommunityMissions(deviceId: String): List<CommunityMissionProgressResponse> {
        val user = userRepository.findByDeviceId(deviceId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }
        val userId = requireNotNull(user.id)

        val communityMissions = communityMissionRepository.findAllByActiveTrue()
            .sortedWith(
                compareBy<CommunityMission> { it.difficulty.order }
                    .thenBy { it.stage }
                    .thenBy { it.id },
            )

        if (communityMissions.isEmpty()) {
            return emptyList()
        }

        val missionIds = communityMissions.map { requireNotNull(it.id) }
        val completionCounts = communityMissionCompletionRepository.countByCommunityMissionIds(missionIds)
            .associate { it.communityMissionId to it.completionCount }
        val completedMissionIds = communityMissionCompletionRepository.findCompletedMissionIdsByUserId(userId).toSet()
        val totalUserCount = userRepository.countByOnboardingCompletedTrue()

        return communityMissions.map { communityMission ->
            val missionId = requireNotNull(communityMission.id)
            val participantCount = completionCounts[missionId] ?: 0L
            val achievementRatio = calculateAchievementRatio(
                participantCount = participantCount,
                totalUserCount = totalUserCount,
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
                succeeded = isSucceeded(achievementRatio, communityMission.targetRatio),
                unlocked = isUnlocked(communityMissions, communityMission, completedMissionIds),
            )
        }
    }

    private fun calculateAchievementRatio(
        participantCount: Long,
        totalUserCount: Long,
    ): BigDecimal {
        if (totalUserCount == 0L) {
            return BigDecimal.ZERO.setScale(2)
        }

        return BigDecimal.valueOf(participantCount)
            .multiply(BigDecimal("100"))
            .divide(BigDecimal.valueOf(totalUserCount), 2, RoundingMode.HALF_UP)
    }

    private fun isSucceeded(
        achievementRatio: BigDecimal,
        targetRatio: BigDecimal,
    ): Boolean {
        return achievementRatio >= targetRatio
    }

    private fun isUnlocked(
        communityMissions: List<CommunityMission>,
        communityMission: CommunityMission,
        completedMissionIds: Set<Long>,
    ): Boolean {
        if (communityMission.stage == 1) {
            return true
        }

        return communityMissions
            .firstOrNull {
                it.difficulty == communityMission.difficulty && it.stage == communityMission.stage - 1
            }
            ?.id in completedMissionIds
    }
}
