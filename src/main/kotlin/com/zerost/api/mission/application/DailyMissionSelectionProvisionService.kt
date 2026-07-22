package com.zerost.api.mission.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.mission.domain.DailyMissionSelection
import com.zerost.api.mission.domain.DailyMissionSelectionRepository
import com.zerost.api.mission.domain.Mission
import com.zerost.api.mission.domain.MissionCategory
import com.zerost.api.mission.domain.MissionRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class DailyMissionSelectionProvisionService(
    private val missionRepository: MissionRepository,
    private val dailyMissionSelectionRepository: DailyMissionSelectionRepository,
    private val dailyMissionSelectionRandomProvider: DailyMissionSelectionRandomProvider,
) {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun createOrLoad(selectedDate: LocalDate): DailyMissionSelections {
        loadSelections(selectedDate)?.let { return it }

        val generalCandidates = missionRepository.findAllByMissionCategoryOrderByIdAsc(MissionCategory.GENERAL)
        val specialCandidates = missionRepository.findAllByMissionCategoryOrderByIdAsc(MissionCategory.SPECIAL)

        if (generalCandidates.size < GENERAL_MISSION_COUNT || specialCandidates.size < SPECIAL_MISSION_COUNT) {
            throw BusinessException(ErrorCode.INVALID_DAILY_MISSION_SELECTION)
        }

        val selectedGeneralMissions = pickDistinctMissions(generalCandidates, GENERAL_MISSION_COUNT)
        val selectedSpecialMission = specialCandidates[dailyMissionSelectionRandomProvider.nextInt(specialCandidates.size)]

        val selections = buildList {
            selectedGeneralMissions.forEachIndexed { index, mission ->
                add(
                    DailyMissionSelection(
                        selectedDate = selectedDate,
                        mission = mission,
                        missionCategory = MissionCategory.GENERAL,
                        displayOrder = index + 1,
                    ),
                )
            }

            add(
                DailyMissionSelection(
                    selectedDate = selectedDate,
                    mission = selectedSpecialMission,
                    missionCategory = MissionCategory.SPECIAL,
                    displayOrder = 1,
                ),
            )
        }

        return try {
            dailyMissionSelectionRepository.saveAllAndFlush(selections)
            DailyMissionSelections(
                generalMissions = selectedGeneralMissions,
                specialMission = selectedSpecialMission,
            )
        } catch (_: DataIntegrityViolationException) {
            loadSelections(selectedDate) ?: throw BusinessException(ErrorCode.INVALID_DAILY_MISSION_SELECTION)
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

    private fun pickDistinctMissions(candidates: List<Mission>, count: Int): List<Mission> {
        val mutableCandidates = candidates.toMutableList()
        return buildList {
            repeat(count) {
                val index = dailyMissionSelectionRandomProvider.nextInt(mutableCandidates.size)
                add(mutableCandidates.removeAt(index))
            }
        }
    }

    companion object {
        private const val GENERAL_MISSION_COUNT = 3
        private const val SPECIAL_MISSION_COUNT = 1
    }
}
