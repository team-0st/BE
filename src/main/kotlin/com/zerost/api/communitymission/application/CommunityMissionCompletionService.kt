package com.zerost.api.communitymission.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.communitymission.domain.CommunityMissionCompletion
import com.zerost.api.communitymission.domain.CommunityMissionCompletionRepository
import com.zerost.api.communitymission.domain.CommunityMissionRepository
import com.zerost.api.communitymission.presentation.dto.CompleteCommunityMissionResponse
import com.zerost.api.user.domain.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class CommunityMissionCompletionService(
    private val userRepository: UserRepository,
    private val communityMissionRepository: CommunityMissionRepository,
    private val communityMissionCompletionRepository: CommunityMissionCompletionRepository,
) {

    @Transactional
    fun complete(deviceId: String, communityMissionId: Long): CompleteCommunityMissionResponse {
        val user = userRepository.findByDeviceIdForUpdate(deviceId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        if (!user.onboardingCompleted) {
            throw BusinessException(ErrorCode.COMMUNITY_MISSION_ONBOARDING_REQUIRED)
        }

        val communityMissions = CommunityMissionUnlockPolicy.sort(communityMissionRepository.findAllByActiveTrue())
        val communityMission = communityMissions.firstOrNull { it.id == communityMissionId }
            ?: throw BusinessException(ErrorCode.COMMUNITY_MISSION_NOT_FOUND)
        val userId = requireNotNull(user.id)
        val completedMissionIds = communityMissionCompletionRepository.findCompletedMissionIdsByUserId(userId).toSet()

        if (!CommunityMissionUnlockPolicy.isUnlocked(communityMissions, communityMission, completedMissionIds)) {
            throw BusinessException(ErrorCode.COMMUNITY_MISSION_NOT_UNLOCKED)
        }

        if (communityMissionCompletionRepository.existsByCommunityMissionIdAndUserId(communityMissionId, userId)) {
            throw BusinessException(ErrorCode.COMMUNITY_MISSION_ALREADY_COMPLETED)
        }

        val completedAt = LocalDateTime.now()
        val completion = communityMissionCompletionRepository.save(
            CommunityMissionCompletion(
                communityMission = communityMission,
                user = user,
                completedAt = completedAt,
            ),
        )

        return CompleteCommunityMissionResponse(
            completionId = requireNotNull(completion.id),
            communityMissionId = requireNotNull(communityMission.id),
            completedAt = completedAt.toString(),
        )
    }
}
