package com.zerost.api.ingredient.domain

import com.zerost.api.user.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface UserIngredientRepository : JpaRepository<UserIngredient, Long> {
    fun findByUserAndIngredient(user: User, ingredient: Ingredient): Optional<UserIngredient>
    fun findByUserIdAndIngredientId(userId: Long, ingredientId: Long): UserIngredient?
    fun findAllByUserIdOrderByIdAsc(userId: Long): List<UserIngredient>
    fun findAllByUserIdAndIngredientIdIn(userId: Long, ingredientIds: Collection<Long>): List<UserIngredient>
}
