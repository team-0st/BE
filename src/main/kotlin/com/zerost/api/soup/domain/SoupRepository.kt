package com.zerost.api.soup.domain

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

interface SoupRepository : JpaRepository<Soup, Long> {
    fun countByUserId(userId: Long): Long

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Soup s where s.id = :soupId")
    fun findByIdForUpdate(@Param("soupId") soupId: Long): Optional<Soup>
}
