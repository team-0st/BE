package com.zerost.api.point.application

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(PointPolicyProperties::class)
class PointPolicyConfig
