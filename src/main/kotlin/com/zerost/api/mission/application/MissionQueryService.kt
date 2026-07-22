package com.zerost.api.mission.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.mission.domain.Mission
import com.zerost.api.mission.domain.MissionCompletion
import com.zerost.api.mission.domain.MissionCompletionRepository
import com.zerost.api.mission.domain.MissionCompletionStatus
import com.zerost.api.mission.domain.MissionRepository
import com.zerost.api.mission.presentation.dto.DailyMissionSectionsResponse
import com.zerost.api.mission.presentation.dto.MissionCompletionHistoryResponse
import com.zerost.api.mission.presentation.dto.MissionDetailResponse
import com.zerost.api.mission.presentation.dto.MissionRewardedIngredientResponse
import com.zerost.api.mission.presentation.dto.MissionSummaryResponse
import com.zerost.api.mission.presentation.dto.MissionTodayStatus
import com.zerost.api.user.domain.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class MissionQueryService(
    private val userRepository: UserRepository,
    private val missionRepository: MissionRepository,
    private val missionCompletionRepository: MissionCompletionRepository,
    private val dailyMissionSelectionService: DailyMissionSelectionService,
    private val clock: Clock,
) {

    @Transactional(readOnly = true)
    fun getMissions(userId: Long): DailyMissionSectionsResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        val todayRange = getTodayRange()
        val selections = dailyMissionSelectionService.getTodaySelections()

        return DailyMissionSectionsResponse(
            generalMissions = selections.generalMissions.map { mission ->
                mission.toSummaryResponse(requireNotNull(user.id), todayRange)
            },
            specialMission = selections.specialMission.toSummaryResponse(requireNotNull(user.id), todayRange),
        )
    }

    @Transactional(readOnly = true)
    fun getMission(userId: Long, missionId: Long): MissionDetailResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        val mission = missionRepository.findById(missionId)
            .orElseThrow { BusinessException(ErrorCode.MISSION_NOT_FOUND) }

        val todayRange = getTodayRange()
        val todayCompletion = missionCompletionRepository
            .findTopByUserIdAndMissionIdAndSubmittedAtBetweenOrderBySubmittedAtDesc(
                userId = requireNotNull(user.id),
                missionId = missionId,
                start = todayRange.first,
                end = todayRange.second,
            )

        return MissionDetailResponse(
            id = requireNotNull(mission.id),
            title = mission.title,
            description = mission.description,
            imageUrl = mission.imageUrl,
            todayStatus = todayCompletion?.status?.toTodayStatus(),
        )
    }

    @Transactional(readOnly = true)
    fun getMissionCompletions(userId: Long): List<MissionCompletionHistoryResponse> {
        val user = userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        return missionCompletionRepository.findAllByUserIdOrderBySubmittedAtDesc(requireNotNull(user.id))
            .map { completion ->
                MissionCompletionHistoryResponse(
                    completionId = requireNotNull(completion.id),
                    missionId = requireNotNull(completion.mission.id),
                    missionTitle = completion.mission.title,
                    status = completion.status.name,
                    rewardedIngredient = completion.rewardedIngredient?.let { ingredient ->
                        MissionRewardedIngredientResponse(
                            id = requireNotNull(ingredient.id),
                            name = ingredient.name,
                            imageUrl = ingredient.imageUrl,
                        )
                    },
                    submittedAt = completion.submittedAt.toString(),
                    reviewedAt = completion.reviewedAt?.toString(),
                )
            }
    }

    private fun getTodayRange(): Pair<LocalDateTime, LocalDateTime> {
        val today = LocalDate.now(clock)
        return today.atStartOfDay() to today.plusDays(1).atStartOfDay()
    }

    private fun Mission.toSummaryResponse(
        userId: Long,
        todayRange: Pair<LocalDateTime, LocalDateTime>,
    ): MissionSummaryResponse {
        val todayCompletion = missionCompletionRepository
            .findTopByUserIdAndMissionIdAndSubmittedAtBetweenOrderBySubmittedAtDesc(
                userId = userId,
                missionId = requireNotNull(id),
                start = todayRange.first,
                end = todayRange.second,
            )

        return MissionSummaryResponse(
            id = requireNotNull(id),
            title = title,
            description = description,
            imageUrl = imageUrl,
            todayStatus = todayCompletion?.status?.toTodayStatus(),
        )
    }

    private fun MissionCompletionStatus.toTodayStatus(): MissionTodayStatus =
        when (this) {
            MissionCompletionStatus.PENDING -> MissionTodayStatus.PENDING
            MissionCompletionStatus.APPROVED -> MissionTodayStatus.APPROVED
            MissionCompletionStatus.REJECTED -> MissionTodayStatus.REJECTED
        }
}
