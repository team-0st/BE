package com.zerost.api.communitymission.application

interface CommunityMissionRewardRandomProvider {
    fun nextInt(bound: Int): Int
}
