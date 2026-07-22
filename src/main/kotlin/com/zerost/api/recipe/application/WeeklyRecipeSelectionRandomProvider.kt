package com.zerost.api.recipe.application

import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class WeeklyRecipeSelectionRandomProvider {
    fun nextInt(bound: Int): Int = Random.nextInt(bound)
}
