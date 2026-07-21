package com.zerost.api.user.domain

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

interface UserRepository : JpaRepository<User, Long> {
    fun findByDeviceId(deviceId: String): Optional<User>
    fun countByOnboardingCompletedTrue(): Long

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :userId")
    fun findByIdForUpdate(@Param("userId") userId: Long): Optional<User>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.deviceId = :deviceId")
    fun findByDeviceIdForUpdate(@Param("deviceId") deviceId: String): Optional<User>
}
