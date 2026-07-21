package com.zerost.api.communitymission.domain

import com.zerost.api.common.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "community_mission_proof_images")
class CommunityMissionProofImage(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_mission_proof_id", nullable = false)
    val proof: CommunityMissionProof,

    @Column(name = "image_key", nullable = false, length = 255)
    val imageKey: String,

    @Column(name = "image_order", nullable = false)
    val imageOrder: Int,
) : BaseEntity()
