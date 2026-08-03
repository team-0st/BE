package com.zerost.api.home.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "홈 화면 통합 조회 응답")
data class HomeResponse(

    @Schema(description = "유저 닉네임", example = "펭귄탐험가", nullable = true)
    val nickname: String?,

    @Schema(description = "선택한 프로필 캐릭터 코드", example = "BROCCOLI", nullable = true)
    val profileCharacterCode: String?,

    @Schema(
        description = "선택한 프로필 캐릭터 이미지 URL",
        example = "https://assets.zero-st.com/profile-characters/broccoli.png",
        nullable = true,
    )
    val profileCharacterImageUrl: String?,

    @Schema(description = "보유 에코잼", example = "320")
    val ecoJam: Int,

    @Schema(description = "보유 포인트", example = "1500")
    val point: Int,

    @Schema(description = "오늘 출석 여부", example = "true")
    val checkedInToday: Boolean,

    @Schema(description = "오늘 미션 진행 현황")
    val missionProgress: HomeMissionProgressResponse,
)
