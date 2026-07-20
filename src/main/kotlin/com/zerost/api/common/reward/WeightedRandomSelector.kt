package com.zerost.api.common.reward

object WeightedRandomSelector {

    fun <T> select(
        candidates: List<WeightedCandidate<T>>,
        nextInt: (Int) -> Int,
    ): T {
        require(candidates.isNotEmpty()) { "추첨 후보는 비어 있을 수 없습니다." }

        val totalWeight = candidates.sumOf { it.weight }
        require(totalWeight > 0) { "추첨 후보의 전체 가중치는 0보다 커야 합니다." }

        val roll = nextInt(totalWeight)
        var cumulativeWeight = 0

        candidates.forEach { candidate ->
            cumulativeWeight += candidate.weight
            if (roll < cumulativeWeight) {
                return candidate.value
            }
        }

        error("가중치 기반 추첨 결과를 계산할 수 없습니다.")
    }
}
