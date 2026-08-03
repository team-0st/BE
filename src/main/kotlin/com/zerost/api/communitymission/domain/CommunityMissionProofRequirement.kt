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
@Table(name = "community_mission_proof_requirements")
class CommunityMissionProofRequirement(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_mission_id", nullable = false)
    val communityMission: CommunityMission,

    @Column(name = "proof_order", nullable = false)
    val proofOrder: Int,

    @Column(length = 100)
    val title: String? = null,

    @Column(columnDefinition = "text")
    val description: String? = null,

    @Column(name = "required_image_count", nullable = false)
    val requiredImageCount: Int,

    @Column(name = "required_day_offset")
    val requiredDayOffset: Int? = null,
) : BaseEntity()
