package com.zerost.api.mission.domain

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.mission.domain.MissionCompletion
import com.zerost.api.mission.domain.MissionCompletionRepository
import com.zerost.api.mission.domain.MissionCompletionStatus
import com.zerost.api.mission.domain.MissionRepository
import com.zerost.api.mission.presentation.dto.SubmitMissionVerificationResponse
import com.zerost.api.user.domain.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class MissionVerificationService(
    private val userRepository: UserRepository,
    private val missionRepository: MissionRepository,
    private val missionCompletionRepository: MissionCompletionRepository,
) {

    @Transactional
    fun submitVerification(
        deviceId: String,
        missionId: Long,
        photoUrl: String,
    ): SubmitMissionVerificationResponse {
        val user = userRepository.findByDeviceIdForUpdate(deviceId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        val mission = missionRepository.findById(missionId)
            .orElseThrow { BusinessException(ErrorCode.MISSION_NOT_FOUND) }

        val (start, end) = todayRange()
        val todayCompletion = missionCompletionRepository
            .findTopByUserIdAndMissionIdAndSubmittedAtBetweenOrderBySubmittedAtDesc(
                userId = requireNotNull(user.id),
                missionId = missionId,
                start = start,
                end = end,
            )

        when (todayCompletion?.status) {
            MissionCompletionStatus.PENDING -> throw BusinessException(ErrorCode.MISSION_UNDER_REVIEW)
            MissionCompletionStatus.APPROVED -> throw BusinessException(ErrorCode.MISSION_ALREADY_COMPLETED)
            MissionCompletionStatus.REJECTED, null -> Unit
        }

        val completion = missionCompletionRepository.save(
            MissionCompletion.submit(
                user = user,
                mission = mission,
                photoUrl = photoUrl,
                submittedAt = LocalDateTime.now(),
            )
        )

        return SubmitMissionVerificationResponse(
            completionId = requireNotNull(completion.id),
            status = completion.status.name,
        )
    }

    private fun todayRange(): Pair<LocalDateTime, LocalDateTime> {
        val today = LocalDate.now()
        return today.atStartOfDay() to today.plusDays(1).atStartOfDay()
    }
}
