package com.zerost.api.communitymission.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.communitymission.domain.CommunityMissionProofRepository
import com.zerost.api.communitymission.domain.CommunityMissionProofStatus
import com.zerost.api.file.application.FileUploadService
import com.zerost.api.support.createCommunityMissionProof
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AdminCommunityMissionReviewQueryServiceTest {

    private val communityMissionProofRepository = mock(CommunityMissionProofRepository::class.java)
    private val fileUploadService = mock(FileUploadService::class.java)
    private val adminCommunityMissionReviewQueryService = AdminCommunityMissionReviewQueryService(
        communityMissionProofRepository = communityMissionProofRepository,
        fileUploadService = fileUploadService,
    )

    @Test
    fun `관리자는 검수 대기 공동 미션 인증 목록을 페이지로 조회할 수 있다`() {
        val proof = createCommunityMissionProof()
        val pageable = PageRequest.of(
            0,
            20,
            Sort.by(
                Sort.Order.asc("submittedAt"),
                Sort.Order.asc("id"),
            ),
        )
        `when`(communityMissionProofRepository.findAllByStatus(CommunityMissionProofStatus.PENDING, pageable))
            .thenReturn(PageImpl(listOf(proof), pageable, 1))
        proof.images.forEach { image ->
            `when`(fileUploadService.createPresignedGetUrl(image.imageKey))
                .thenReturn("https://example.com/${image.imageKey}?signed=1")
        }

        val response = adminCommunityMissionReviewQueryService.getPendingProofs(page = 0, size = 20)

        assertEquals(1, response.items.size)
        assertEquals(0, response.page)
        assertEquals(20, response.size)
        assertEquals(1, response.totalElements)
        assertEquals(1, response.totalPages)
        assertEquals(false, response.hasNext)
        assertEquals(response.items[0].imageKeys.size, response.items[0].imageUrls.size)
        assertEquals(
            response.items[0].imageKeys.map { "https://example.com/$it?signed=1" },
            response.items[0].imageUrls,
        )
    }

    @Test
    fun `유효하지 않은 페이지 요청이면 예외가 발생한다`() {
        val exception = assertFailsWith<BusinessException> {
            adminCommunityMissionReviewQueryService.getPendingProofs(page = -1, size = 20)
        }

        assertEquals(ErrorCode.INVALID_INPUT_VALUE, exception.errorCode)
    }
}
