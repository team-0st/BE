package com.zerost.api.communitymission.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.communitymission.domain.CommunityMissionProofRepository
import com.zerost.api.communitymission.presentation.dto.ReviewCommunityMissionProofResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class AdminCommunityMissionReviewService(
    private val communityMissionProofRepository: CommunityMissionProofRepository,
) {

    @Transactional
    fun reviewProof(
        proofId: Long,
        status: String,
    ): ReviewCommunityMissionProofResponse {
        val proof = communityMissionProofRepository.findByIdForUpdate(proofId)
            ?: throw BusinessException(ErrorCode.COMMUNITY_MISSION_PROOF_NOT_FOUND)

        val reviewedAt = LocalDateTime.now()
        when (status) {
            "APPROVED" -> proof.approve(reviewedAt)
            "REJECTED" -> proof.reject(reviewedAt)
            else -> throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)
        }

        return ReviewCommunityMissionProofResponse(
            proofId = requireNotNull(proof.id),
            status = proof.status.name,
            reviewedAt = requireNotNull(proof.reviewedAt).toString(),
        )
    }
}
