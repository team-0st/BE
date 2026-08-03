package com.zerost.api.gacha.domain

import org.springframework.data.jpa.repository.JpaRepository

interface GachaRepository : JpaRepository<Gacha, Long> {
    fun findAllByUserIdOrderByCreatedAtDescIdDesc(userId: Long): List<Gacha>
}
