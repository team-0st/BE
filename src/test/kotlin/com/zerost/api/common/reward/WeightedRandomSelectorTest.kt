package com.zerost.api.common.reward

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class WeightedRandomSelectorTest {

    @Test
    fun `누적 가중치 구간에 따라 후보를 선택한다`() {
        val candidates = listOf(
            WeightedCandidate(value = "A", weight = 5),
            WeightedCandidate(value = "B", weight = 10),
            WeightedCandidate(value = "C", weight = 20),
        )

        val first = WeightedRandomSelector.select(candidates) { 0 }
        val second = WeightedRandomSelector.select(candidates) { 5 }
        val third = WeightedRandomSelector.select(candidates) { 34 }

        assertEquals("A", first)
        assertEquals("B", second)
        assertEquals("C", third)
    }

    @Test
    fun `후보가 비어 있으면 예외가 발생한다`() {
        assertThrows<IllegalArgumentException> {
            WeightedRandomSelector.select<String>(emptyList()) { 0 }
        }
    }
}
