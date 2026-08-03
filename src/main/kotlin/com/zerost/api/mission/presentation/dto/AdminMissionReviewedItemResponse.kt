package com.zerost.api.mission.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "관리자 미션 검수 완료 항목 응답")
data class AdminMissionReviewedItemResponse(

    @field:Schema(description = "미션 인증 ID", example = "12")
    val completionId: Long,

    @field:Schema(description = "유저 ID", example = "3")
    val userId: Long,

    @field:Schema(description = "유저 닉네임", example = "펭귄탐험가", nullable = true)
    val userNickname: String?,

    @field:Schema(description = "미션 ID", example = "1")
    val missionId: Long,

    @field:Schema(description = "미션 제목", example = "텀블러 사용하기")
    val missionTitle: String,

    @field:Schema(description = "검수 결과", example = "APPROVED", allowableValues = ["APPROVED", "REJECTED"])
    val status: String,

    @field:Schema(
        description = "검수 대상 인증 이미지의 S3 객체 키",
        example = "missions/1/1/2026/07/18/550e8400-e29b-41d4-a716-446655440000.jpg",
    )
    val photoKey: String,

    @field:Schema(description = "검수 미리보기용 S3 presigned GET URL")
    val photoUrl: String,

    @field:Schema(description = "제출 시각", example = "2026-07-18T10:00:00")
    val submittedAt: String,

    @field:Schema(description = "검수 시각", example = "2026-07-18T11:00:00", nullable = true)
    val reviewedAt: String?,
)
