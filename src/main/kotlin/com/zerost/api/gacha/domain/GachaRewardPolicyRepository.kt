package com.zerost.api.gacha.domain

import org.springframework.data.jpa.repository.JpaRepository

interface GachaRewardPolicyRepository : JpaRepository<GachaRewardPolicy, Long> {
    fun findAllByActiveTrueOrderByIdAsc(): List<GachaRewardPolicy>
}
