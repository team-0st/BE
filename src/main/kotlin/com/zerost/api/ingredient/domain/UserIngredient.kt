package com.zerost.api.ingredient.domain

import com.zerost.api.common.entity.BaseEntity
import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.user.domain.User
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
@Table(name = "user_ingredients")
class UserIngredient(

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
    var quantity: Int = 0,
) : BaseEntity() {

    fun increaseQuantity(amount: Int = 1) {
        this.quantity += amount
    }

    fun decreaseQuantity(amount: Int = 1) {
        if (this.quantity < amount) {
            throw BusinessException(ErrorCode.INSUFFICIENT_INGREDIENT_QUANTITY)
        }
        this.quantity -= amount
    }
}
