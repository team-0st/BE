package com.zerost.api.communitymission.domain

import org.springframework.data.jpa.repository.JpaRepository

interface CommunityMissionRewardRepository : JpaRepository<CommunityMissionReward, Long> {
    fun findAllByCommunityMissionIdOrderByRewardOrderAsc(communityMissionId: Long): List<CommunityMissionReward>
}
