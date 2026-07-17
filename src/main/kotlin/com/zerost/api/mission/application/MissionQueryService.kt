package com.zerost.api.mission.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.mission.domain.Mission
import com.zerost.api.mission.domain.MissionCompletion
import com.zerost.api.mission.domain.MissionCompletionRepository
import com.zerost.api.mission.domain.MissionCompletionStatus
import com.zerost.api.mission.domain.MissionRepository
import com.zerost.api.mission.presentation.dto.MissionDetailResponse
import com.zerost.api.mission.presentation.dto.MissionSummaryResponse
import com.zerost.api.mission.presentation.dto.MissionTodayStatus
import com.zerost.api.user.domain.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class MissionQueryService(
    private val userRepository: UserRepository,
    private val missionRepository: MissionRepository,
    private val missionCompletionRepository: MissionCompletionRepository,
) {

    @Transactional(readOnly = true)
    fun getMissions(deviceId: String): List<MissionSummaryResponse> {
        val user = userRepository.findByDeviceId(deviceId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        val todayRange = getTodayRange()

        return missionRepository.findAll()
            .map { mission ->
                val todayCompletion = missionCompletionRepository
                    .findTopByUserIdAndMissionIdAndSubmittedAtBetweenOrderBySubmittedAtDesc(
                        userId = requireNotNull(user.id),
                        missionId = requireNotNull(mission.id),
                        start = todayRange.first,
                        end = todayRange.second,
                    )

                MissionSummaryResponse(
                    id = requireNotNull(mission.id),
                    title = mission.title,
                    description = mission.description,
                    imageUrl = mission.imageUrl,
                    todayStatus = todayCompletion?.status?.toTodayStatus(),
                )
            }
    }

    @Transactional(readOnly = true)
    fun getMission(deviceId: String, missionId: Long): MissionDetailResponse {
        val user = userRepository.findByDeviceId(deviceId)
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

    private fun getTodayRange(): Pair<LocalDateTime, LocalDateTime> {
        val today = LocalDate.now()
        return today.atStartOfDay() to today.plusDays(1).atStartOfDay()
    }

    private fun MissionCompletionStatus.toTodayStatus(): MissionTodayStatus =
        when (this) {
            MissionCompletionStatus.PENDING -> MissionTodayStatus.PENDING
            MissionCompletionStatus.APPROVED -> MissionTodayStatus.APPROVED
            MissionCompletionStatus.REJECTED -> MissionTodayStatus.REJECTED
        }
}
