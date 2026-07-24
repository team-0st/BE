package com.zerost.api.point.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PointHistoryRepository : JpaRepository<PointHistory, Long> {
    fun findAllByUserIdOrderByCreatedAtDescIdDesc(userId: Long): List<PointHistory>

    @Query(
        """
        select coalesce(sum(ph.amount), 0)
        from PointHistory ph
        where ph.user.id = :userId
          and ph.amount > 0
        """,
    )
    fun sumEarnedAmountByUserId(@Param("userId") userId: Long): Int
}
