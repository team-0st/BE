package com.zerost.api.mission.domain

import com.zerost.api.common.entity.BaseEntity
import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "missions")
class Mission(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, length = 100)
    var title: String,

    @Column(columnDefinition = "text")
    var description: String? = null,

    @Column(name = "image_url", length = 255)
    var imageUrl: String? = null,

    @Column(name = "reward_ingredient_pool", nullable = false, columnDefinition = "json")
    var rewardIngredientPool: String,
) : BaseEntity() {
    fun extractRewardIngredientIds(): List<Long> {
        val trimmedPool = rewardIngredientPool.trim().removeSurrounding("\"")
        if (!trimmedPool.startsWith("[") || !trimmedPool.endsWith("]")) {
            throw BusinessException(ErrorCode.INVALID_MISSION_REWARD_POOL)
        }

        val body = trimmedPool.removePrefix("[").removeSuffix("]").trim()
        if (body.isBlank()) {
            throw BusinessException(ErrorCode.INVALID_MISSION_REWARD_POOL)
        }

        return body.split(",")
            .map { token ->
                token.trim()
                    .takeIf { it.isNotBlank() }
                    ?.toLongOrNull()
                    ?: throw BusinessException(ErrorCode.INVALID_MISSION_REWARD_POOL)
            }
    }
}
