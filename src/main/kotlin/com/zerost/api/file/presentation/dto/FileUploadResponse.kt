package com.zerost.api.file.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "파일 업로드 응답")
data class FileUploadResponse(

    @field:Schema(
        description = "업로드 직후 미리보기에 사용할 presigned 조회 URL",
        example = "https://example-bucket.s3.ap-northeast-2.amazonaws.com/missions/device-1/1/2026/07/18/550e8400-e29b-41d4-a716-446655440000.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256",
    )
    val fileUrl: String,

    @field:Schema(
        description = "S3 객체 키",
        example = "missions/device-1/1/2026/07/18/550e8400-e29b-41d4-a716-446655440000.jpg",
    )
    val fileKey: String,
)
