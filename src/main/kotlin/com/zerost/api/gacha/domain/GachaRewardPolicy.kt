package com.zerost.api.gacha.domain

import com.zerost.api.common.entity.BaseEntity
import com.zerost.api.ingredient.domain.Ingredient
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
import java.math.BigDecimal

@Entity
@Table(name = "gacha_reward_policies")
class GachaRewardPolicy(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, length = 100)
    val name: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "reward_type", nullable = false, length = 30)
    val rewardType: GachaRewardType,

    @Column(nullable = false, precision = 5, scale = 2)
    val probability: BigDecimal,

    @Column(name = "point_amount", nullable = false)
    val pointAmount: Int = 0,

    @Column(name = "eco_jam_amount", nullable = false)
    val ecoJamAmount: Int = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id")
    val ingredient: Ingredient? = null,

    @Column(name = "ingredient_quantity", nullable = false)
    val ingredientQuantity: Int = 0,

    @Column(name = "active", nullable = false)
    val active: Boolean = true,
) : BaseEntity() {

    init {
        require(probability >= BigDecimal.ZERO) { "가챠 확률은 0 이상이어야 합니다." }

        when (rewardType) {
            GachaRewardType.FAIL -> {
                require(pointAmount == 0) { "FAIL 보상은 포인트를 지급할 수 없습니다." }
                require(ecoJamAmount == 0) { "FAIL 보상은 에코잼을 지급할 수 없습니다." }
                require(ingredient == null) { "FAIL 보상은 재료를 지급할 수 없습니다." }
                require(ingredientQuantity == 0) { "FAIL 보상은 재료 수량을 가질 수 없습니다." }
            }

            GachaRewardType.ECO_JAM -> {
                require(pointAmount == 0) { "ECO_JAM 보상은 포인트를 함께 지급할 수 없습니다." }
                require(ecoJamAmount > 0) { "ECO_JAM 보상은 에코잼 수량이 0보다 커야 합니다." }
                require(ingredient == null) { "ECO_JAM 보상은 재료를 함께 지급할 수 없습니다." }
                require(ingredientQuantity == 0) { "ECO_JAM 보상은 재료 수량을 가질 수 없습니다." }
            }

            GachaRewardType.POINT -> {
                require(pointAmount > 0) { "POINT 보상은 포인트 수량이 0보다 커야 합니다." }
                require(ecoJamAmount == 0) { "POINT 보상은 에코잼을 함께 지급할 수 없습니다." }
                require(ingredient == null) { "POINT 보상은 재료를 함께 지급할 수 없습니다." }
                require(ingredientQuantity == 0) { "POINT 보상은 재료 수량을 가질 수 없습니다." }
            }

            GachaRewardType.INGREDIENT -> {
                require(pointAmount == 0) { "INGREDIENT 보상은 포인트를 함께 지급할 수 없습니다." }
                require(ecoJamAmount == 0) { "INGREDIENT 보상은 에코잼을 함께 지급할 수 없습니다." }
                require(ingredient != null) { "INGREDIENT 보상은 재료가 필수입니다." }
                require(ingredientQuantity > 0) { "INGREDIENT 보상은 재료 수량이 0보다 커야 합니다." }
            }
        }
    }
}
