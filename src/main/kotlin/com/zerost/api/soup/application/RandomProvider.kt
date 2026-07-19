package com.zerost.api.soup.application

import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class RandomProvider {
    fun nextInt(until: Int): Int = Random.nextInt(until)
}
