package com.zerost.api.mission.domain

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime
import java.util.Optional

interface MissionCompletionRepository : JpaRepository<MissionCompletion, Long> {
    fun findAllByUserIdOrderBySubmittedAtDesc(userId: Long): List<MissionCompletion>

    fun findAllByUserIdAndSubmittedAtBetween(
        userId: Long,
        start: LocalDateTime,
        end: LocalDateTime,
    ): List<MissionCompletion>

    fun findTopByUserIdAndMissionIdAndSubmittedAtBetweenOrderBySubmittedAtDesc(
        userId: Long,
        missionId: Long,
        start: LocalDateTime,
        end: LocalDateTime,
    ): MissionCompletion?

    fun countByUserIdAndStatus(userId: Long, status: MissionCompletionStatus): Long

    @EntityGraph(attributePaths = ["user", "mission"])
    fun findAllByStatusOrderBySubmittedAtAsc(status: MissionCompletionStatus): List<MissionCompletion>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select mc from MissionCompletion mc where mc.id = :completionId")
    fun findByIdForUpdate(@Param("completionId") completionId: Long): Optional<MissionCompletion>
}
