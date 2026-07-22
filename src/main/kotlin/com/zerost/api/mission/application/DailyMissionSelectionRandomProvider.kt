package com.zerost.api.mission.application

import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class DailyMissionSelectionRandomProvider {
    fun nextInt(bound: Int): Int = Random.nextInt(bound)
}
