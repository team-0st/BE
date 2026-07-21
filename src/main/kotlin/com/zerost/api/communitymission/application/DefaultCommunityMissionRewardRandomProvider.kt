package com.zerost.api.communitymission.application

import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class DefaultCommunityMissionRewardRandomProvider : CommunityMissionRewardRandomProvider {
    override fun nextInt(bound: Int): Int = Random.nextInt(bound)
}
