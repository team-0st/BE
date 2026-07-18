package com.zerost.api.common.config

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties(prefix = "app.storage.s3")
data class S3Properties(
    @field:NotBlank
    val bucket: String,

    @field:NotBlank
    val region: String,

    @field:Positive
    val maxFileSize: Long,

    @field:Positive
    val presignedUrlDurationSeconds: Long,
)
