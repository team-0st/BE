package com.zerost.api.gacha.domain

import com.zerost.api.common.entity.BaseEntity
import com.zerost.api.ingredient.domain.Ingredient
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
import java.math.BigDecimal

@Entity
@Table(name = "gacha_reward_policies")
class GachaRewardPolicy(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, length = 100)
    val name: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "reward_type", nullable = false, length = 30)
    val rewardType: GachaRewardType,

    @Column(nullable = false, precision = 5, scale = 2)
    val probability: BigDecimal,

    @Column(name = "point_amount", nullable = false)
    val pointAmount: Int = 0,

    @Column(name = "eco_jam_amount", nullable = false)
    val ecoJamAmount: Int = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id")
    val ingredient: Ingredient? = null,

    @Column(name = "ingredient_quantity", nullable = false)
    val ingredientQuantity: Int = 0,

    @Column(name = "active", nullable = false)
    val active: Boolean = true,
) : BaseEntity()
