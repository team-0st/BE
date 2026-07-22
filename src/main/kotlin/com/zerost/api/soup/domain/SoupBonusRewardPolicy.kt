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
import java.math.BigDecimal

@Entity
@Table(name = "soup_bonus_reward_policies")
class SoupBonusRewardPolicy(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "recipe_type", nullable = false, length = 20)
    val recipeType: RecipeType,

    @Enumerated(EnumType.STRING)
    @Column(name = "reward_grade", nullable = false, length = 30)
    val rewardGrade: SoupRewardGrade,

    @Column(nullable = false, precision = 5, scale = 2)
    val probability: BigDecimal,

    @Column(name = "point_amount", nullable = false)
    val pointAmount: Int = 0,

    @Column(name = "eco_jam_amount", nullable = false)
    val ecoJamAmount: Int = 0,

    @Column(nullable = false)
    val active: Boolean = true,

    @OneToMany(mappedBy = "soupBonusRewardPolicy", fetch = FetchType.LAZY)
    val ingredients: MutableList<SoupBonusRewardPolicyIngredient> = mutableListOf(),
) : BaseEntity()
