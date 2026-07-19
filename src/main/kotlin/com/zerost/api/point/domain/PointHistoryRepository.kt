package com.zerost.api.point.domain

import org.springframework.data.jpa.repository.JpaRepository

interface PointHistoryRepository : JpaRepository<PointHistory, Long>
