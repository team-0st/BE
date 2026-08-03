package com.zerost.api.mission.domain

import org.springframework.data.jpa.repository.JpaRepository

interface MissionRepository : JpaRepository<Mission, Long> {
    fun findAllByMissionCategoryOrderByIdAsc(missionCategory: MissionCategory): List<Mission>
}
