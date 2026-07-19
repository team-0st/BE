package com.zerost.api.soup.presentation.dto

data class SoupRewardSummary(
    val rewardGrade: String,
    val ecoJam: Int,
    val point: Int,
    val rewardedIngredients: List<SoupRewardIngredientResponse>,
)
