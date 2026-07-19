package com.zerost.api.gacha.application

import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class GachaRandomProvider {
    fun nextInt(until: Int): Int = Random.nextInt(until)
}
