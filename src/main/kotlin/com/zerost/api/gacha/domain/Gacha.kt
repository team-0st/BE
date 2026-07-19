package com.zerost.api.gacha.domain

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

@Entity
@Table(name = "gachas")
class Gacha(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reward_policy_id", nullable = false)
    val rewardPolicy: GachaRewardPolicy,

    @Column(name = "cost_eco_jam", nullable = false)
    val costEcoJam: Int,

    @Enumerated(EnumType.STRING)
    @Column(name = "result_type", nullable = false, length = 30)
    val resultType: GachaRewardType,

    @Column(name = "result_point", nullable = false)
    var resultPoint: Int = 0,

    @Column(name = "result_eco_jam", nullable = false)
    var resultEcoJam: Int = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "result_ingredient_id")
    var resultIngredient: Ingredient? = null,

    @Column(name = "result_ingredient_quantity", nullable = false)
    var resultIngredientQuantity: Int = 0,
) : BaseEntity()
