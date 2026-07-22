package com.zerost.api.mission.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "오늘의 미션 섹션형 응답")
data class DailyMissionSectionsResponse(

    @Schema(description = "일반 미션 목록")
    val generalMissions: List<MissionSummaryResponse>,

    @Schema(description = "특별 미션")
    val specialMission: MissionSummaryResponse?,
)
