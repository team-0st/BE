package com.zerost.api.point.application

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.point.policy")
data class PointPolicyProperties(
    val maxCumulativeEarnAmountPerUser: String? = null,
) {

    init {
        resolvedMaxCumulativeEarnAmountPerUser()
    }

    fun resolvedMaxCumulativeEarnAmountPerUser(): Int? {
        val rawValue = maxCumulativeEarnAmountPerUser
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: return null

        val parsedValue = rawValue.toIntOrNull()
            ?: throw IllegalArgumentException("포인트 누적 지급 상한 설정값은 0 이상의 정수여야 합니다. value=$rawValue")

        require(parsedValue >= 0) {
            "포인트 누적 지급 상한 설정값은 0 이상이어야 합니다. value=$rawValue"
        }

        return parsedValue
    }
}
