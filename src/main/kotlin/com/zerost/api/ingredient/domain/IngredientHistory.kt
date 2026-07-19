package com.zerost.api.ingredient.domain

import com.zerost.api.common.entity.BaseEntity
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
@Table(name = "ingredient_histories")
class IngredientHistory(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    val ingredient: Ingredient,

    @Column(nullable = false)
    val amount: Int,

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 30)
    val sourceType: IngredientHistorySourceType,

    @Column(name = "source_id", nullable = false)
    val sourceId: Long,
) : BaseEntity() {

    companion object {
        fun earn(
            user: User,
            ingredient: Ingredient,
            amount: Int,
            sourceType: IngredientHistorySourceType,
            sourceId: Long,
        ): IngredientHistory {
            require(amount > 0) { "재료 적립 이력 수량은 0보다 커야 합니다." }

            return IngredientHistory(
                user = user,
                ingredient = ingredient,
                amount = amount,
                sourceType = sourceType,
                sourceId = sourceId,
            )
        }
    }
}
