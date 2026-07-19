package com.zerost.api.ecojam.domain

import org.springframework.data.jpa.repository.JpaRepository

interface EcoJamHistoryRepository : JpaRepository<EcoJamHistory, Long> {
    fun findAllByUserIdOrderByCreatedAtDesc(userId: Long): List<EcoJamHistory>
}
