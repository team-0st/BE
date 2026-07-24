package com.zerost.api.communitymission.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "관리자 공동 미션 검수 완료 목록 페이지")
data class AdminCommunityMissionProofReviewedPageResponse(
    val items: List<AdminCommunityMissionProofReviewedItemResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
)
