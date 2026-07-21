package com.zerost.api.communitymission.domain

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface CommunityMissionRepository : JpaRepository<CommunityMission, Long> {
    fun findAllByActiveTrue(): List<CommunityMission>
    fun findByIdAndActiveTrue(id: Long): CommunityMission?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select cm from CommunityMission cm where cm.id = :id and cm.active = true")
    fun findByIdAndActiveTrueForUpdate(@Param("id") id: Long): CommunityMission?
}
