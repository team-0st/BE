package com.zerost.api.ingredient.domain

import org.springframework.data.jpa.repository.JpaRepository

interface IngredientHistoryRepository : JpaRepository<IngredientHistory, Long> {
    fun findAllByUserIdOrderByCreatedAtDescIdDesc(userId: Long): List<IngredientHistory>
}
