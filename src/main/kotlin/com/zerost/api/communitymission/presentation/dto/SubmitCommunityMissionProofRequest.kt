package com.zerost.api.communitymission.presentation.dto

import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

@Schema(description = "공동 미션 인증 제출 요청")
data class SubmitCommunityMissionProofRequest(

    @field:NotEmpty(message = "photoKeys는 최소 1개 이상이어야 합니다.")
    @field:ArraySchema(
        schema = Schema(
            description = "파일 업로드 API로 업로드한 공동 미션 인증 이미지 파일 키",
            example = "community-missions/1/3/2026/07/21/550e8400-e29b-41d4-a716-446655440000.jpg",
        ),
    )
    @field:Size(max = 5, message = "photoKeys는 최대 5개까지 제출할 수 있습니다.")
    val photoKeys: List<@Size(max = 255, message = "photoKey는 255자 이하여야 합니다.") String>,
)
