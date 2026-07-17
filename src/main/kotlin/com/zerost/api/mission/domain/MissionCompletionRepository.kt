package com.zerost.api.mission.domain

import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface MissionCompletionRepository : JpaRepository<MissionCompletion, Long> {
    fun findAllByUserIdOrderBySubmittedAtDesc(userId: Long): List<MissionCompletion>

    fun findTopByUserIdAndMissionIdAndSubmittedAtBetweenOrderBySubmittedAtDesc(
        userId: Long,
        missionId: Long,
        start: LocalDateTime,
        end: LocalDateTime,
    ): MissionCompletion?
}
