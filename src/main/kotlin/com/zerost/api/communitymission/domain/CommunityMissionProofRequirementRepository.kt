package com.zerost.api.communitymission.domain

import org.springframework.data.jpa.repository.JpaRepository

interface CommunityMissionProofRequirementRepository : JpaRepository<CommunityMissionProofRequirement, Long> {
    fun findAllByCommunityMissionIdOrderByProofOrderAsc(communityMissionId: Long): List<CommunityMissionProofRequirement>
    fun findByIdAndCommunityMissionId(id: Long, communityMissionId: Long): CommunityMissionProofRequirement?
}
