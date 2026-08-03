package com.zerost.api.mission.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.mission.domain.DailyMissionSelectionRepository
import com.zerost.api.mission.domain.Mission
import com.zerost.api.mission.domain.MissionCategory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.LocalDate

@Service
class DailyMissionSelectionService(
    private val dailyMissionSelectionRepository: DailyMissionSelectionRepository,
    private val dailyMissionSelectionProvisionService: DailyMissionSelectionProvisionService,
    private val clock: Clock,
) {

    fun getTodaySelections(): DailyMissionSelections {
        val today = LocalDate.now(clock)
        val existingSelections = loadSelections(today)
        if (existingSelections != null) {
            return existingSelections
        }

        return try {
            dailyMissionSelectionProvisionService.create(today)
        } catch (_: DataIntegrityViolationException) {
            loadSelections(today) ?: throw BusinessException(ErrorCode.INVALID_DAILY_MISSION_SELECTION)
        }
    }

    private fun loadSelections(selectedDate: LocalDate): DailyMissionSelections? {
        val generalSelections = dailyMissionSelectionRepository
            .findAllBySelectedDateAndMissionCategoryOrderByDisplayOrderAsc(selectedDate, MissionCategory.GENERAL)
        val specialSelections = dailyMissionSelectionRepository
            .findAllBySelectedDateAndMissionCategoryOrderByDisplayOrderAsc(selectedDate, MissionCategory.SPECIAL)

        if (generalSelections.isEmpty() && specialSelections.isEmpty()) {
            return null
        }

        if (generalSelections.size != GENERAL_MISSION_COUNT || specialSelections.size != SPECIAL_MISSION_COUNT) {
            throw BusinessException(ErrorCode.INVALID_DAILY_MISSION_SELECTION)
        }

        return DailyMissionSelections(
            generalMissions = generalSelections.map { it.mission },
            specialMission = specialSelections.first().mission,
        )
    }

    companion object {
        private const val GENERAL_MISSION_COUNT = 3
        private const val SPECIAL_MISSION_COUNT = 1
    }
}

data class DailyMissionSelections(
    val generalMissions: List<Mission>,
    val specialMission: Mission,
)
