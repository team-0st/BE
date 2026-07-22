package com.zerost.api.common.config

import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties(prefix = "app.public-assets")
data class PublicAssetsProperties(
    @field:NotBlank
    val baseUrl: String,
)
