package com.zerost.api.communitymission.application

import com.zerost.api.communitymission.domain.CommunityMissionProofRepository
import com.zerost.api.communitymission.domain.CommunityMissionProofStatus
import com.zerost.api.communitymission.presentation.dto.AdminCommunityMissionProofReviewItemResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminCommunityMissionReviewQueryService(
    private val communityMissionProofRepository: CommunityMissionProofRepository,
) {

    @Transactional(readOnly = true)
    fun getPendingProofs(): List<AdminCommunityMissionProofReviewItemResponse> {
        return communityMissionProofRepository.findAllByStatusOrderBySubmittedAtAsc(CommunityMissionProofStatus.PENDING)
            .map { proof ->
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
                    imageKeys = proof.images.sortedBy { it.imageOrder }.map { it.imageKey },
                )
            }
    }
}
