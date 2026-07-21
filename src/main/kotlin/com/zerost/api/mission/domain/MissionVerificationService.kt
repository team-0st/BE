package com.zerost.api.mission.domain

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.file.application.FileUploadService
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
    private val fileUploadService: FileUploadService,
) {

    @Transactional
    fun submitVerification(
        userId: Long,
        missionId: Long,
        photoKey: String,
    ): SubmitMissionVerificationResponse {
        val user = userRepository.findByIdForUpdate(userId)
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

        fileUploadService.validateMissionImageKey(userId, missionId, photoKey)

        val completion = missionCompletionRepository.save(
            MissionCompletion.submit(
                user = user,
                mission = mission,
                photoKey = photoKey,
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
