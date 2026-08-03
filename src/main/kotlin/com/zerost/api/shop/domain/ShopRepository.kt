package com.zerost.api.shop.domain

import org.springframework.data.jpa.repository.JpaRepository

interface ShopRepository : JpaRepository<Shop, Long> {
    fun findAllByOrderByIdAsc(): List<Shop>
}
