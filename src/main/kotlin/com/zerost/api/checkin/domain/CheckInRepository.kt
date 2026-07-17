package com.zerost.api.checkin.domain

import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface CheckInRepository : JpaRepository<CheckIn, Long> {
    fun existsByUserIdAndCheckedDate(userId: Long, checkedDate: LocalDate): Boolean
}
