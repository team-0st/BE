package com.zerost.api.testerlink.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "테스트 링크 갱신 요청")
data class UpdateTesterLinkRequest(

    @field:NotBlank
    @Schema(
        description = "앱인토스 콘솔 QR/링크 전체",
        example = "intoss-private://0st?_deploymentId=019f893b-a962-71de-b3ea-2c2544ad7afa",
    )
    val deepLink: String,
)
