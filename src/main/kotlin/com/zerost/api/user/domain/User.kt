package com.zerost.api.user.domain

import com.zerost.api.common.entity.BaseEntity
import com.zerost.api.shop.domain.Shop
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "users")
class User(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "device_id", nullable = false, unique = true, length = 64)
    val deviceId: String,

    @Column(length = 50)
    var nickname: String? = null,

    @Column(name = "phone_number", length = 20)
    var phoneNumber: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    var shop: Shop? = null,

    @Column(name = "eco_jam", nullable = false)
    var ecoJam: Int = 0,

    @Column(name = "onboarding_completed", nullable = false)
    var onboardingCompleted: Boolean = false,
) : BaseEntity() {

    fun completeOnboarding(
        nickname: String,
        phoneNumber: String,
        shop: Shop
    ) {
        this.nickname = nickname
        this.phoneNumber = phoneNumber
        this.shop = shop
        this.onboardingCompleted = true
    }
}
