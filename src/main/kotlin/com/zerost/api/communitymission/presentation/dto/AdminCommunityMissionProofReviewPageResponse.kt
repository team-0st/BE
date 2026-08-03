package com.zerost.api.communitymission.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "관리자 공동 미션 검수 대기 목록 페이지 응답")
data class AdminCommunityMissionProofReviewPageResponse(
    @Schema(description = "검수 대기 항목 목록")
    val items: List<AdminCommunityMissionProofReviewItemResponse>,
    @Schema(description = "현재 페이지 번호", example = "0")
    val page: Int,
    @Schema(description = "페이지 크기", example = "20")
    val size: Int,
    @Schema(description = "전체 항목 수", example = "57")
    val totalElements: Long,
    @Schema(description = "전체 페이지 수", example = "3")
    val totalPages: Int,
    @Schema(description = "다음 페이지 존재 여부", example = "true")
    val hasNext: Boolean,
)
