package com.zerost.api.communitymission.domain

import org.springframework.data.jpa.repository.JpaRepository

interface CommunityMissionRepository : JpaRepository<CommunityMission, Long> {
    fun findAllByActiveTrue(): List<CommunityMission>
    fun findByIdAndActiveTrue(id: Long): CommunityMission?
}
