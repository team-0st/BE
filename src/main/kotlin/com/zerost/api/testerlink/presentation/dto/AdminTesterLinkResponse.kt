package com.zerost.api.testerlink.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "관리자 테스트 링크 응답")
data class AdminTesterLinkResponse(

    @Schema(description = "테스터에게 공유할 고정 URL", example = "https://zero-st.com/open")
    val shareUrl: String,

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

    @Schema(
        description = "토스 https 공유 링크 (getTossShareLink 결과)",
        example = "https://toss.im/_m/abcdef",
        nullable = true,
    )
    val tossShareUrl: String?,

    @Schema(description = "마지막 갱신 시각", example = "2026-07-23T01:30:00", nullable = true)
    val updatedAt: String?,
)
