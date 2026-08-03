package com.zerost.api.recipe.domain

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface WeeklyRecipeSelectionRepository : JpaRepository<WeeklyRecipeSelection, Long> {

    @EntityGraph(attributePaths = ["recipe"])
    fun findByWeekStartDate(weekStartDate: LocalDate): WeeklyRecipeSelection?
}
