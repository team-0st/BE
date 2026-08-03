package com.zerost.api.soup.domain

import com.zerost.api.common.entity.BaseEntity
import com.zerost.api.recipe.domain.RecipeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "soup_reroll_policy_groups")
class SoupRerollPolicyGroup(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "recipe_type", nullable = false, length = 20)
    val recipeType: RecipeType,

    @Enumerated(EnumType.STRING)
    @Column(name = "current_reward_grade", nullable = false, length = 30)
    val currentRewardGrade: SoupRewardGrade,

    @Column(name = "reroll_cost_eco_jam", nullable = false)
    val rerollCostEcoJam: Int,

    @Column(nullable = false)
    val active: Boolean = true,

    @OneToMany(mappedBy = "soupRerollPolicyGroup", fetch = FetchType.LAZY)
    val candidates: MutableList<SoupRerollPolicyCandidate> = mutableListOf(),
) : BaseEntity()
