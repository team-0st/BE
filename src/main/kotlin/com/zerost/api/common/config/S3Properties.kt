package com.zerost.api.common.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.storage.s3")
data class S3Properties(
    val bucket: String,
    val region: String,
)
