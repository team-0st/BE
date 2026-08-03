package com.zerost.api.checkin.application

import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class CheckInRandomProvider {
    fun nextInt(until: Int): Int = Random.nextInt(until)
}
