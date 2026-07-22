package com.zerost.api.communitymission.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.communitymission.domain.CommunityMissionProofRepository
import com.zerost.api.communitymission.domain.CommunityMissionProofStatus
import com.zerost.api.support.createCommunityMissionProof
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AdminCommunityMissionReviewServiceTest {
    private val reviewerId = 99L

    private val communityMissionProofRepository = mock(CommunityMissionProofRepository::class.java)
    private val adminCommunityMissionReviewService = AdminCommunityMissionReviewService(
        communityMissionProofRepository = communityMissionProofRepository,
    )

    @Test
    fun `관리자는 공동 미션 인증을 승인할 수 있다`() {
        val proof = createCommunityMissionProof(status = CommunityMissionProofStatus.PENDING)
        `when`(communityMissionProofRepository.findByIdForUpdate(1L)).thenReturn(proof)

        val response = adminCommunityMissionReviewService.reviewProof(reviewerId, 1L, "APPROVED")

        assertEquals("APPROVED", response.status)
        assertEquals(CommunityMissionProofStatus.APPROVED, proof.status)
    }

    @Test
    fun `이미 검수된 공동 미션 인증은 다시 검수할 수 없다`() {
        val proof = createCommunityMissionProof(status = CommunityMissionProofStatus.APPROVED)
        `when`(communityMissionProofRepository.findByIdForUpdate(1L)).thenReturn(proof)

        val exception = assertFailsWith<BusinessException> {
            adminCommunityMissionReviewService.reviewProof(reviewerId, 1L, "REJECTED")
        }

        assertEquals(ErrorCode.INVALID_COMMUNITY_MISSION_PROOF_REVIEW_STATUS, exception.errorCode)
    }
}
