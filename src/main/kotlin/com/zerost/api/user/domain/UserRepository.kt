package com.zerost.api.user.domain

import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface UserRepository : JpaRepository<User, Long> {
    fun findByDeviceId(deviceId: String): Optional<User>
}
