package com.zerost.api.soup.domain

import com.zerost.api.common.entity.BaseEntity
import com.zerost.api.ingredient.domain.Ingredient
import com.zerost.api.ingredient.domain.IngredientType
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
@Table(name = "soup_reroll_policy_ingredients")
class SoupRerollPolicyIngredient(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "soup_reroll_policy_candidate_id", nullable = false)
    val soupRerollPolicyCandidate: SoupRerollPolicyCandidate,

    @Enumerated(EnumType.STRING)
    @Column(name = "selection_type", nullable = false, length = 30)
    val selectionType: SoupRewardIngredientSelectionType,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id")
    val ingredient: Ingredient? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "ingredient_type", length = 20)
    val ingredientType: IngredientType? = null,

    @Column(nullable = false)
    val quantity: Int,
) : BaseEntity()
