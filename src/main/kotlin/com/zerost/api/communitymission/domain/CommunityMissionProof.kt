package com.zerost.api.communitymission.domain

import com.zerost.api.common.entity.BaseEntity
import com.zerost.api.user.domain.User
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
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

    @Column(name = "submitted_at", nullable = false)
    val submittedAt: LocalDateTime,

    @OneToMany(mappedBy = "proof", cascade = [CascadeType.ALL], orphanRemoval = true)
    val images: MutableList<CommunityMissionProofImage> = mutableListOf(),
) : BaseEntity() {

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
}
