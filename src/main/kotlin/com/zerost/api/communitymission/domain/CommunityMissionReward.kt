package com.zerost.api.communitymission.domain

import com.zerost.api.common.entity.BaseEntity
import com.zerost.api.ingredient.domain.IngredientType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "community_mission_rewards")
class CommunityMissionReward(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_mission_id", nullable = false)
    val communityMission: CommunityMission,

    @Enumerated(EnumType.STRING)
    @Column(name = "reward_type", nullable = false, length = 30)
    val rewardType: CommunityMissionRewardType,

    @Enumerated(EnumType.STRING)
    @Column(name = "ingredient_type", length = 20)
    val ingredientType: IngredientType? = null,

    @Column(nullable = false)
    val quantity: Int = 0,

    @Column(name = "eco_jam_amount", nullable = false)
    val ecoJamAmount: Int = 0,

    @Column(name = "reward_order", nullable = false)
    val rewardOrder: Int,
) : BaseEntity() {

    init {
        when (rewardType) {
            CommunityMissionRewardType.ECO_JAM -> {
                require(ingredientType == null) { "에코잼 보상에는 재료 타입이 없어야 합니다." }
                require(quantity == 0) { "에코잼 보상 수량은 0이어야 합니다." }
                require(ecoJamAmount > 0) { "에코잼 보상 금액은 0보다 커야 합니다." }
            }

            CommunityMissionRewardType.INGREDIENT -> {
                require(ingredientType != null) { "재료 보상에는 재료 타입이 필요합니다." }
                require(quantity > 0) { "재료 보상 수량은 0보다 커야 합니다." }
                require(ecoJamAmount == 0) { "재료 보상은 에코잼을 함께 지급할 수 없습니다." }
            }
        }
    }
}
