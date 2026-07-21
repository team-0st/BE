package com.zerost.api.communitymission.domain

import com.zerost.api.common.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "community_missions")
class CommunityMission(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, length = 100)
    var title: String,

    @Column(columnDefinition = "text")
    var description: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var difficulty: CommunityMissionDifficulty,

    @Column(nullable = false)
    var stage: Int,

    @Column(name = "target_ratio", nullable = false, precision = 5, scale = 2)
    var targetRatio: BigDecimal,

    @Column(name = "image_url", length = 255)
    var imageUrl: String? = null,

    @Column(name = "is_active", nullable = false)
    var active: Boolean = true,
) : BaseEntity()
