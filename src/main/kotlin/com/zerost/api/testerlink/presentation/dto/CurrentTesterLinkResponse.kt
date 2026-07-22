package com.zerost.api.testerlink.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "공개 테스트 링크 응답")
data class CurrentTesterLinkResponse(

    @Schema(
        description = "현재 deep link",
        example = "intoss-private://0st?_deploymentId=019f893b-a962-71de-b3ea-2c2544ad7afa",
        nullable = true,
    )
    val deepLink: String?,

    @Schema(
        description = "deploymentId",
        example = "019f893b-a962-71de-b3ea-2c2544ad7afa",
        nullable = true,
    )
    val deploymentId: String?,
)
