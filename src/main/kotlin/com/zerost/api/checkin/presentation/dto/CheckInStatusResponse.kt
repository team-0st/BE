package com.zerost.api.checkin.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "오늘 출석 여부 응답")
data class CheckInStatusResponse(

    @Schema(description = "오늘 출석 여부", example = "true")
    val checkedIn: Boolean,
)
