package com.zerost.api.gacha.domain

import com.zerost.api.support.createIngredient
import com.zerost.api.ingredient.domain.IngredientType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class GachaRewardPolicyTest {

    @Test
    fun `POINT 보상은 포인트만 가질 수 있다`() {
        assertThrows<IllegalArgumentException> {
            GachaRewardPolicy(
                id = 1L,
                name = "잘못된 포인트 정책",
                rewardType = GachaRewardType.POINT,
                probability = BigDecimal("5.00"),
                pointAmount = 100,
                ecoJamAmount = 30,
            )
        }
    }

    @Test
    fun `INGREDIENT 보상은 재료와 수량이 필수다`() {
        assertThrows<IllegalArgumentException> {
            GachaRewardPolicy(
                id = 2L,
                name = "잘못된 재료 정책",
                rewardType = GachaRewardType.INGREDIENT,
                probability = BigDecimal("10.00"),
                ingredient = createIngredient(id = 1L),
                ingredientQuantity = 0,
            )
        }
    }

    @Test
    fun `INGREDIENT 보상은 재료 타입 기반 랜덤 지급도 허용한다`() {
        GachaRewardPolicy(
            id = 3L,
            name = "랜덤 일반 재료 1개",
            rewardType = GachaRewardType.INGREDIENT,
            probability = BigDecimal("10.00"),
            ingredientType = IngredientType.COMMON,
            ingredientQuantity = 1,
        )
    }

    @Test
    fun `INGREDIENT 보상은 대표 재료와 재료 타입을 함께 두고 랜덤 지급 정책으로 사용할 수 있다`() {
        GachaRewardPolicy(
            id = 4L,
            name = "랜덤 일반 재료 2개",
            rewardType = GachaRewardType.INGREDIENT,
            probability = BigDecimal("10.00"),
            ingredient = createIngredient(id = 1L),
            ingredientType = IngredientType.COMMON,
            ingredientQuantity = 2,
        )
    }
}
