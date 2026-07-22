package com.zerost.api.mission.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.mission.domain.DailyMissionSelection
import com.zerost.api.mission.domain.DailyMissionSelectionRepository
import com.zerost.api.mission.domain.Mission
import com.zerost.api.mission.domain.MissionCategory
import com.zerost.api.mission.domain.MissionRepository
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
    fun create(selectedDate: LocalDate): DailyMissionSelections {
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

        dailyMissionSelectionRepository.saveAllAndFlush(selections)
        return DailyMissionSelections(
            generalMissions = selectedGeneralMissions,
            specialMission = selectedSpecialMission,
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
