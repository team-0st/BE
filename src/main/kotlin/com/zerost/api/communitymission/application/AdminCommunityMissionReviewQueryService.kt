package com.zerost.api.communitymission.application

import com.zerost.api.communitymission.domain.CommunityMissionProofRepository
import com.zerost.api.communitymission.domain.CommunityMissionProofStatus
import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.communitymission.presentation.dto.AdminCommunityMissionProofReviewItemResponse
import com.zerost.api.communitymission.presentation.dto.AdminCommunityMissionProofReviewPageResponse
import com.zerost.api.file.application.FileUploadService
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminCommunityMissionReviewQueryService(
    private val communityMissionProofRepository: CommunityMissionProofRepository,
    private val fileUploadService: FileUploadService,
) {
    companion object {
        private const val MAX_PAGE_SIZE = 100
    }

    @Transactional(readOnly = true)
    fun getPendingProofs(
        page: Int,
        size: Int,
    ): AdminCommunityMissionProofReviewPageResponse {
        validatePageRequest(page, size)

        val pageable = PageRequest.of(
            page,
            size,
            Sort.by(
                Sort.Order.asc("submittedAt"),
                Sort.Order.asc("id"),
            ),
        )
        val proofPage = communityMissionProofRepository.findAllByStatus(CommunityMissionProofStatus.PENDING, pageable)

        return AdminCommunityMissionProofReviewPageResponse(
            items = proofPage.content.map { proof ->
                val imageKeys = proof.images.sortedBy { it.imageOrder }.map { it.imageKey }
                AdminCommunityMissionProofReviewItemResponse(
                    proofId = requireNotNull(proof.id),
                    communityMissionId = requireNotNull(proof.communityMission.id),
                    communityMissionTitle = proof.communityMission.title,
                    requirementId = requireNotNull(proof.proofRequirement.id),
                    proofOrder = proof.proofRequirement.proofOrder,
                    requirementTitle = proof.proofRequirement.title,
                    userId = requireNotNull(proof.user.id),
                    nickname = proof.user.nickname,
                    submittedAt = proof.submittedAt.toString(),
                    imageKeys = imageKeys,
                    imageUrls = imageKeys.map { fileUploadService.createPresignedGetUrl(it) },
                )
            },
            page = proofPage.number,
            size = proofPage.size,
            totalElements = proofPage.totalElements,
            totalPages = proofPage.totalPages,
            hasNext = proofPage.hasNext(),
        )
    }

    private fun validatePageRequest(
        page: Int,
        size: Int,
    ) {
        if (page < 0 || size <= 0 || size > MAX_PAGE_SIZE) {
            throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)
        }
    }
}
