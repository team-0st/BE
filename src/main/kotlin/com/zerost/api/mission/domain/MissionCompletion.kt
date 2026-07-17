package com.zerost.api.mission.domain

import com.zerost.api.common.entity.BaseEntity
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
    var photoUrl: String,

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
) : BaseEntity()
