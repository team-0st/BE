package com.zerost.api.mypage.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "마이페이지 통합 조회 응답")
data class MyPageResponse(

    @Schema(description = "유저 닉네임", example = "펭귄탐험가", nullable = true)
    val nickname: String?,

    @Schema(description = "선택한 프로필 캐릭터 코드", example = "BASIC_1", nullable = true)
    val profileCharacterCode: String?,

    @Schema(description = "선택한 상점명", example = "알맹상점", nullable = true)
    val shopName: String?,

    @Schema(description = "보유 에코잼", example = "320")
    val ecoJam: Int,

    @Schema(description = "보유 포인트", example = "1500")
    val point: Int,

    @Schema(description = "지금까지 제작한 스프 수", example = "5")
    val brewedSoupCount: Int,

    @Schema(description = "지금까지 승인된 미션 수", example = "7")
    val completedMissionCount: Int,

    @Schema(description = "보유 중인 전체 재료 수량 합계", example = "12")
    val totalIngredientQuantity: Int,

    @Schema(description = "보유 재료 목록")
    val ingredients: List<MyPageIngredientResponse>,
)
