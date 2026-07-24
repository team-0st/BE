package com.zerost.api.point.application

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.point.policy")
data class PointPolicyProperties(
    val maxCumulativeEarnAmountPerUser: Int? = null,
)
