package com.zerost.api.mission.domain

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.file.application.FileUploadService
import com.zerost.api.mission.domain.MissionCompletion
import com.zerost.api.mission.domain.MissionCompletionRepository
import com.zerost.api.mission.domain.MissionCompletionStatus
import com.zerost.api.mission.domain.MissionRepository
import com.zerost.api.mission.presentation.dto.DeleteMissionVerificationResponse
import com.zerost.api.mission.presentation.dto.SubmitMissionVerificationResponse
import com.zerost.api.mission.presentation.dto.UpdateMissionVerificationResponse
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

    @Transactional
    fun updateVerification(
        userId: Long,
        completionId: Long,
        photoKey: String,
    ): UpdateMissionVerificationResponse {
        val user = userRepository.findByIdForUpdate(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }
        val completion = missionCompletionRepository.findByIdForUpdate(completionId)
            .orElseThrow { BusinessException(ErrorCode.MISSION_COMPLETION_NOT_FOUND) }

        if (!completion.belongsTo(requireNotNull(user.id))) {
            throw BusinessException(ErrorCode.MISSION_COMPLETION_NOT_FOUND)
        }

        fileUploadService.validateMissionImageKey(
            userId = requireNotNull(user.id),
            missionId = requireNotNull(completion.mission.id),
            fileKey = photoKey,
        )

        val previousPhotoKey = completion.photoKey
        completion.updatePhotoKey(photoKey)

        if (previousPhotoKey != photoKey) {
            fileUploadService.delete(previousPhotoKey)
        }

        return UpdateMissionVerificationResponse(
            completionId = requireNotNull(completion.id),
            missionId = requireNotNull(completion.mission.id),
            status = completion.status.name,
            photoKey = completion.photoKey,
        )
    }

    @Transactional
    fun deleteVerification(
        userId: Long,
        completionId: Long,
    ): DeleteMissionVerificationResponse {
        val user = userRepository.findByIdForUpdate(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }
        val completion = missionCompletionRepository.findByIdForUpdate(completionId)
            .orElseThrow { BusinessException(ErrorCode.MISSION_COMPLETION_NOT_FOUND) }

        if (!completion.belongsTo(requireNotNull(user.id))) {
            throw BusinessException(ErrorCode.MISSION_COMPLETION_NOT_FOUND)
        }

        completion.validateDeletable()
        missionCompletionRepository.delete(completion)
        fileUploadService.delete(completion.photoKey)

        return DeleteMissionVerificationResponse(
            completionId = completionId,
        )
    }

    private fun todayRange(): Pair<LocalDateTime, LocalDateTime> {
        val today = LocalDate.now()
        return today.atStartOfDay() to today.plusDays(1).atStartOfDay()
    }
}
