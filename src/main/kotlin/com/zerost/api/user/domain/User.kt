package com.zerost.api.user.domain

import com.zerost.api.common.entity.BaseEntity
import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.shop.domain.Shop
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table

@Entity
@Table(name = "users")
class User(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(length = 50)
    var nickname: String? = null,

    @Column(name = "phone_number", length = 20)
    var phoneNumber: String? = null,

    @Column(name = "password_hash", length = 255)
    var passwordHash: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "profile_character_code", length = 50)
    var profileCharacterCode: ProfileCharacterCode? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    var shop: Shop? = null,

    @Column(name = "eco_jam", nullable = false)
    var ecoJam: Int = 0,

    @Column(name = "point", nullable = false)
    var point: Int = 0,

    @Column(name = "onboarding_completed", nullable = false)
    var onboardingCompleted: Boolean = false,
) : BaseEntity() {

    fun completeOnboarding(
        nickname: String,
        phoneNumber: String,
        passwordHash: String,
        shop: Shop
    ) {
        this.nickname = nickname
        this.phoneNumber = phoneNumber
        this.passwordHash = passwordHash
        this.shop = shop
        this.onboardingCompleted = true
    }

    fun changeProfileCharacter(profileCharacterCode: ProfileCharacterCode) {
        this.profileCharacterCode = profileCharacterCode
    }

    fun increaseEcoJam(amount: Int) {
        this.ecoJam += amount
    }

    fun decreaseEcoJam(amount: Int) {
        require(amount > 0) { "차감할 에코잼은 0보다 커야 합니다." }
        if (this.ecoJam < amount) {
            throw BusinessException(ErrorCode.INSUFFICIENT_ECO_JAM)
        }
        this.ecoJam -= amount
    }

    fun increasePoint(amount: Int) {
        this.point += amount
    }

    fun decreasePoint(amount: Int) {
        require(amount > 0) { "차감할 포인트는 0보다 커야 합니다." }
        if (this.point < amount) {
            throw BusinessException(ErrorCode.INSUFFICIENT_POINT)
        }
        this.point -= amount
    }
}
