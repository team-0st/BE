package com.zerost.api.mission.application

import com.zerost.api.file.application.FileUploadService
import com.zerost.api.mission.domain.MissionCompletionRepository
import com.zerost.api.mission.domain.MissionCompletionStatus
import com.zerost.api.mission.presentation.dto.AdminMissionReviewItemResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminMissionReviewQueryService(
    private val missionCompletionRepository: MissionCompletionRepository,
    private val fileUploadService: FileUploadService,
) {

    @Transactional(readOnly = true)
    fun getPendingMissionCompletions(): List<AdminMissionReviewItemResponse> {
        return missionCompletionRepository.findAllByStatusOrderBySubmittedAtAsc(MissionCompletionStatus.PENDING)
            .map { completion ->
                AdminMissionReviewItemResponse(
                    completionId = requireNotNull(completion.id),
                    userId = requireNotNull(completion.user.id),
                    userNickname = completion.user.nickname,
                    missionId = requireNotNull(completion.mission.id),
                    missionTitle = completion.mission.title,
                    photoKey = completion.photoKey,
                    photoUrl = fileUploadService.createPresignedGetUrl(completion.photoKey),
                    submittedAt = completion.submittedAt.toString(),
                )
            }
    }
}
