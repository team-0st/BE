package com.zerost.api.communitymission.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface CommunityMissionCompletionRepository : JpaRepository<CommunityMissionCompletion, Long> {

    fun existsByCommunityMissionIdAndUserId(communityMissionId: Long, userId: Long): Boolean

    @Query(
        """
        select count(cmc)
        from CommunityMissionCompletion cmc
        where cmc.communityMission.id = :communityMissionId
          and cmc.user.onboardingCompleted = true
        """
    )
    fun countByCommunityMissionId(@Param("communityMissionId") communityMissionId: Long): Long

    @EntityGraph(attributePaths = ["user"])
    fun findAllByCommunityMissionIdAndRewardedAtIsNullOrderByIdAsc(communityMissionId: Long): List<CommunityMissionCompletion>

    @EntityGraph(attributePaths = ["user"])
    fun findTop100ByCommunityMissionIdAndRewardedAtIsNullOrderByIdAsc(communityMissionId: Long): List<CommunityMissionCompletion>

    @Query(
        """
        select distinct cmc.communityMission.id
        from CommunityMissionCompletion cmc
        where cmc.rewardedAt is null
          and cmc.communityMission.succeededAt is not null
        """
    )
    fun findDistinctSucceededCommunityMissionIdsWithPendingRewards(): List<Long>

    @Query(
        """
        select
            cmc.communityMission.id as communityMissionId,
            count(cmc) as completionCount
        from CommunityMissionCompletion cmc
        where cmc.communityMission.id in :missionIds
          and cmc.user.onboardingCompleted = true
        group by cmc.communityMission.id
        """
    )
    fun countByCommunityMissionIds(@Param("missionIds") missionIds: Collection<Long>): List<CommunityMissionCompletionCountProjection>

    @Query(
        """
        select cmc.communityMission.id
        from CommunityMissionCompletion cmc
        where cmc.user.id = :userId
        """
    )
    fun findCompletedMissionIdsByUserId(@Param("userId") userId: Long): List<Long>
}
