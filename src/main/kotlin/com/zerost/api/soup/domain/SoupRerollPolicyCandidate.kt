package com.zerost.api.soup.domain

import com.zerost.api.common.entity.BaseEntity
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
import java.math.BigDecimal

@Entity
@Table(name = "soup_reroll_policy_candidates")
class SoupRerollPolicyCandidate(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "soup_reroll_policy_group_id", nullable = false)
    val soupRerollPolicyGroup: SoupRerollPolicyGroup,

    @Enumerated(EnumType.STRING)
    @Column(name = "next_reward_grade", nullable = false, length = 30)
    val nextRewardGrade: SoupRewardGrade,

    @Column(nullable = false, precision = 5, scale = 2)
    val probability: BigDecimal,

    @Column(name = "point_amount", nullable = false)
    val pointAmount: Int = 0,

    @Column(name = "eco_jam_amount", nullable = false)
    val ecoJamAmount: Int = 0,

    @Column(nullable = false)
    val active: Boolean = true,

    @OneToMany(mappedBy = "soupRerollPolicyCandidate", fetch = FetchType.LAZY)
    val ingredients: MutableList<SoupRerollPolicyIngredient> = mutableListOf(),
) : BaseEntity()
