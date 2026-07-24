package com.zerost.api.communitymission.domain

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface CommunityMissionProofRepository : JpaRepository<CommunityMissionProof, Long> {
    fun countByCommunityMissionIdAndUserId(communityMissionId: Long, userId: Long): Long
    fun countByCommunityMissionIdAndUserIdAndStatus(
        communityMissionId: Long,
        userId: Long,
        status: CommunityMissionProofStatus,
    ): Long

    fun findByProofRequirementIdAndUserId(proofRequirementId: Long, userId: Long): CommunityMissionProof?

    @EntityGraph(attributePaths = ["proofRequirement", "images"])
    fun findAllByCommunityMissionIdAndUserIdOrderByProofRequirementProofOrderAsc(
        communityMissionId: Long,
        userId: Long,
    ): List<CommunityMissionProof>

    @EntityGraph(attributePaths = ["communityMission", "proofRequirement", "user", "images"])
    fun findAllByStatusOrderBySubmittedAtAsc(status: CommunityMissionProofStatus): List<CommunityMissionProof>

    @EntityGraph(attributePaths = ["communityMission", "proofRequirement", "user"])
    fun findAllByStatus(status: CommunityMissionProofStatus, pageable: Pageable): Page<CommunityMissionProof>

    @EntityGraph(attributePaths = ["communityMission", "proofRequirement", "user", "images"])
    fun findAllByStatusIn(statuses: Collection<CommunityMissionProofStatus>, pageable: Pageable): Page<CommunityMissionProof>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select cmp from CommunityMissionProof cmp where cmp.id = :proofId")
    fun findByIdForUpdate(@Param("proofId") proofId: Long): CommunityMissionProof?

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

    @Query(
        """
        select
            cmp.communityMission.id as communityMissionId,
            count(cmp) as proofCount
        from CommunityMissionProof cmp
        where cmp.user.id = :userId
          and cmp.status = :status
          and cmp.communityMission.id in :communityMissionIds
        group by cmp.communityMission.id
        """
    )
    fun countByUserIdAndCommunityMissionIdsAndStatus(
        @Param("userId") userId: Long,
        @Param("status") status: CommunityMissionProofStatus,
        @Param("communityMissionIds") communityMissionIds: Collection<Long>,
    ): List<CommunityMissionProofCountProjection>
}
