package com.zerost.api.common.reward

data class WeightedCandidate<T>(
    val value: T,
    val weight: Int,
) {
    init {
        require(weight > 0) { "가중치는 0보다 커야 합니다." }
    }
}
