package com.zerost.api.mission.domain

import com.zerost.api.common.entity.BaseEntity
import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.ingredient.domain.Ingredient
import com.zerost.api.user.domain.User
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
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "mission_completions")
class MissionCompletion(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    val mission: Mission,

    @Column(name = "photo_url", nullable = false, length = 255)
    var photoKey: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: MissionCompletionStatus,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rewarded_ingredient_id")
    var rewardedIngredient: Ingredient? = null,

    @Column(name = "submitted_at", nullable = false)
    val submittedAt: LocalDateTime,

    @Column(name = "reviewed_at")
    var reviewedAt: LocalDateTime? = null,

    @Column(name = "reward_claimed_at")
    var rewardClaimedAt: LocalDateTime? = null,
) : BaseEntity() {
    companion object {
        fun submit(
            user: User,
            mission: Mission,
            photoKey: String,
            submittedAt: LocalDateTime,
        ): MissionCompletion {
            return MissionCompletion(
                user = user,
                mission = mission,
                photoKey = photoKey,
                status = MissionCompletionStatus.PENDING,
                submittedAt = submittedAt,
            )
        }
    }

    fun approve(reviewedAt: LocalDateTime) {
        validatePendingStatus()
        this.status = MissionCompletionStatus.APPROVED
        this.reviewedAt = reviewedAt
    }

    fun reject(reviewedAt: LocalDateTime) {
        validatePendingStatus()
        this.status = MissionCompletionStatus.REJECTED
        this.reviewedAt = reviewedAt
    }

    fun assignRewardedIngredient(ingredient: Ingredient) {
        this.rewardedIngredient = ingredient
    }

    fun validateClaimable() {
        if (this.status != MissionCompletionStatus.APPROVED || this.rewardedIngredient == null) {
            throw BusinessException(ErrorCode.MISSION_REWARD_CLAIM_NOT_AVAILABLE)
        }
        if (this.rewardClaimedAt != null) {
            throw BusinessException(ErrorCode.MISSION_REWARD_ALREADY_CLAIMED)
        }
    }

    fun markRewardClaimed(claimedAt: LocalDateTime) {
        validateClaimable()
        this.rewardClaimedAt = claimedAt
    }

    fun isRewardClaimed(): Boolean = rewardClaimedAt != null

    fun isRewardClaimable(): Boolean =
        this.status == MissionCompletionStatus.APPROVED &&
            this.rewardedIngredient != null &&
            this.rewardClaimedAt == null

    fun validateEditable() {
        validateEditableStatus()
    }

    fun updatePhotoKey(photoKey: String): Boolean {
        validateEditableStatus()

        if (this.status == MissionCompletionStatus.REJECTED) {
            this.status = MissionCompletionStatus.PENDING
            this.reviewedAt = null
        }

        if (this.photoKey == photoKey) {
            return false
        }

        this.photoKey = photoKey

        return true
    }

    fun validateDeletable() {
        validateEditableStatus()
    }

    fun belongsTo(userId: Long): Boolean = requireNotNull(user.id) == userId

    private fun validatePendingStatus() {
        if (this.status != MissionCompletionStatus.PENDING) {
            throw BusinessException(ErrorCode.INVALID_MISSION_REVIEW_STATUS)
        }
    }

    private fun validateEditableStatus() {
        if (this.status == MissionCompletionStatus.APPROVED) {
            throw BusinessException(ErrorCode.MISSION_COMPLETION_MODIFICATION_NOT_ALLOWED)
        }
    }
}
