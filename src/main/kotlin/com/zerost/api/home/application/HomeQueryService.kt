package com.zerost.api.home.application

import com.zerost.api.checkin.domain.CheckInRepository
import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.profile.application.ProfileCharacterImageUrlResolver
import com.zerost.api.home.presentation.dto.HomeMissionProgressResponse
import com.zerost.api.home.presentation.dto.HomeResponse
import com.zerost.api.mission.domain.MissionCompletionStatus
import com.zerost.api.mission.domain.MissionRepository
import com.zerost.api.mission.domain.MissionCompletionRepository
import com.zerost.api.user.domain.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class HomeQueryService(
    private val userRepository: UserRepository,
    private val checkInRepository: CheckInRepository,
    private val missionRepository: MissionRepository,
    private val missionCompletionRepository: MissionCompletionRepository,
    private val profileCharacterImageUrlResolver: ProfileCharacterImageUrlResolver,
) {

    @Transactional(readOnly = true)
    fun getHome(userId: Long): HomeResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }
        val resolvedUserId = requireNotNull(user.id)
        val today = LocalDate.now()
        val todayRange = getTodayRange(today)
        val todayCompletions = missionCompletionRepository.findAllByUserIdAndSubmittedAtGreaterThanEqualAndSubmittedAtLessThan(
            userId = resolvedUserId,
            start = todayRange.first,
            end = todayRange.second,
        )

        return HomeResponse(
            nickname = user.nickname,
            profileCharacterCode = user.profileCharacterCode?.name,
            profileCharacterImageUrl = profileCharacterImageUrlResolver.resolveOrNull(user.profileCharacterCode),
            ecoJam = user.ecoJam,
            point = user.point,
            checkedInToday = checkInRepository.existsByUserIdAndCheckedDate(resolvedUserId, today),
            missionProgress = HomeMissionProgressResponse(
                totalMissionCount = missionRepository.count().toInt(),
                submittedMissionCount = todayCompletions.size,
                pendingMissionCount = todayCompletions.count { it.status == MissionCompletionStatus.PENDING },
                approvedMissionCount = todayCompletions.count { it.status == MissionCompletionStatus.APPROVED },
                rejectedMissionCount = todayCompletions.count { it.status == MissionCompletionStatus.REJECTED },
            ),
        )
    }

    private fun getTodayRange(today: LocalDate): Pair<LocalDateTime, LocalDateTime> {
        return today.atStartOfDay() to today.plusDays(1).atStartOfDay()
    }
}
