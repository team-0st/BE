package com.zerost.api.communitymission.domain

import com.zerost.api.common.entity.BaseEntity
import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.user.domain.User
import org.hibernate.annotations.BatchSize
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "community_mission_proofs")
class CommunityMissionProof(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_mission_id", nullable = false)
    val communityMission: CommunityMission,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proof_requirement_id", nullable = false)
    val proofRequirement: CommunityMissionProofRequirement,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: CommunityMissionProofStatus,

    @Column(name = "submitted_at", nullable = false)
    var submittedAt: LocalDateTime,

    @Column(name = "reviewed_at")
    var reviewedAt: LocalDateTime? = null,

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "proof", cascade = [CascadeType.ALL], orphanRemoval = true)
    val images: MutableList<CommunityMissionProofImage> = mutableListOf(),
) : BaseEntity() {

    companion object {
        fun submit(
            communityMission: CommunityMission,
            proofRequirement: CommunityMissionProofRequirement,
            user: User,
            submittedAt: LocalDateTime,
        ): CommunityMissionProof {
            return CommunityMissionProof(
                communityMission = communityMission,
                proofRequirement = proofRequirement,
                user = user,
                status = CommunityMissionProofStatus.PENDING,
                submittedAt = submittedAt,
            )
        }
    }

    fun addImage(
        imageKey: String,
        imageOrder: Int,
    ) {
        images += CommunityMissionProofImage(
            proof = this,
            imageKey = imageKey,
            imageOrder = imageOrder,
        )
    }

    fun approve(reviewedAt: LocalDateTime) {
        validatePendingStatus()
        this.status = CommunityMissionProofStatus.APPROVED
        this.reviewedAt = reviewedAt
    }

    fun reject(reviewedAt: LocalDateTime) {
        validatePendingStatus()
        this.status = CommunityMissionProofStatus.REJECTED
        this.reviewedAt = reviewedAt
    }

    fun canResubmit(): Boolean = this.status == CommunityMissionProofStatus.REJECTED

    fun resubmit(
        submittedAt: LocalDateTime,
        imageKeys: List<String>,
    ) {
        if (!canResubmit()) {
            throw BusinessException(ErrorCode.COMMUNITY_MISSION_PROOF_ALREADY_SUBMITTED)
        }

        this.status = CommunityMissionProofStatus.PENDING
        this.submittedAt = submittedAt
        this.reviewedAt = null
        this.images.clear()
        imageKeys.forEachIndexed { index, imageKey ->
            addImage(
                imageKey = imageKey,
                imageOrder = index + 1,
            )
        }
    }

    fun isApproved(): Boolean = this.status == CommunityMissionProofStatus.APPROVED

    private fun validatePendingStatus() {
        if (this.status != CommunityMissionProofStatus.PENDING) {
            throw BusinessException(ErrorCode.INVALID_COMMUNITY_MISSION_PROOF_REVIEW_STATUS)
        }
    }
}
