package com.zerost.api.common.config

import com.zerost.api.auth.application.AuthTokenProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(AuthTokenProperties::class)
class AuthConfig
