package com.zerost.api.mission.domain

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface DailyMissionSelectionRepository : JpaRepository<DailyMissionSelection, Long> {

    @EntityGraph(attributePaths = ["mission"])
    fun findAllBySelectedDateAndMissionCategoryOrderByDisplayOrderAsc(
        selectedDate: LocalDate,
        missionCategory: MissionCategory,
    ): List<DailyMissionSelection>
}
