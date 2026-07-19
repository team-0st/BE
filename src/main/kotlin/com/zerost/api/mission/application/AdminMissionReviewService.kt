package com.zerost.api.mission.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.mission.domain.MissionCompletionRepository
import com.zerost.api.mission.presentation.dto.ReviewMissionCompletionResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class AdminMissionReviewService(
    private val missionCompletionRepository: MissionCompletionRepository,
) {

    @Transactional
    fun reviewMissionCompletion(
        completionId: Long,
        status: String,
    ): ReviewMissionCompletionResponse {
        val completion = missionCompletionRepository.findById(completionId)
            .orElseThrow { BusinessException(ErrorCode.MISSION_COMPLETION_NOT_FOUND) }

        val reviewedAt = LocalDateTime.now()

        when (status) {
            "APPROVED" -> completion.approve(reviewedAt)
            "REJECTED" -> completion.reject(reviewedAt)
            else -> throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)
        }

        return ReviewMissionCompletionResponse(
            completionId = requireNotNull(completion.id),
            status = completion.status.name,
            reviewedAt = requireNotNull(completion.reviewedAt).toString(),
        )
    }
}
