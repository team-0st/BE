package com.zerost.api.communitymission.domain

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface CommunityMissionProofRepository : JpaRepository<CommunityMissionProof, Long> {
    fun existsByProofRequirementIdAndUserId(proofRequirementId: Long, userId: Long): Boolean
    fun countByCommunityMissionIdAndUserId(communityMissionId: Long, userId: Long): Long

    @EntityGraph(attributePaths = ["proofRequirement", "images"])
    fun findAllByCommunityMissionIdAndUserIdOrderByProofRequirementProofOrderAsc(
        communityMissionId: Long,
        userId: Long,
    ): List<CommunityMissionProof>

    @Query(
        """
        select
            cmp.communityMission.id as communityMissionId,
            count(cmp) as proofCount
        from CommunityMissionProof cmp
        where cmp.user.id = :userId
          and cmp.communityMission.id in :communityMissionIds
        group by cmp.communityMission.id
        """
    )
    fun countByUserIdAndCommunityMissionIds(
        @Param("userId") userId: Long,
        @Param("communityMissionIds") communityMissionIds: Collection<Long>,
    ): List<CommunityMissionProofCountProjection>
}
