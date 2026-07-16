package com.zerost.api.shop.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "샵 목록 응답 항목")
data class ShopResponse(

    @Schema(description = "샵 식별자", example = "1")
    val id: Long,

    @Schema(description = "샵 이름", example = "알맹상점 성수점")
    val name: String,

    @Schema(description = "샵 설명", example = "제로웨이스트 생활용품을 판매하는 샵")
    val description: String?,

    @Schema(description = "샵 이미지 URL", example = "https://example.com/images/shop-1.png")
    val imageUrl: String?,
)
