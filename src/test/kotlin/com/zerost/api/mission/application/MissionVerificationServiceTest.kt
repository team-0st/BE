package com.zerost.api.mission.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.file.application.FileUploadService
import com.zerost.api.mission.domain.MissionCompletionRepository
import com.zerost.api.mission.domain.MissionCompletionStatus
import com.zerost.api.mission.domain.MissionRepository
import com.zerost.api.mission.domain.MissionVerificationService
import com.zerost.api.support.createMission
import com.zerost.api.support.createMissionCompletion
import com.zerost.api.support.createUser
import com.zerost.api.user.domain.UserRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional
import kotlin.test.assertEquals

class MissionVerificationServiceTest {

    private val userRepository = mock(UserRepository::class.java)
    private val missionRepository = mock(MissionRepository::class.java)
    private val missionCompletionRepository = mock(MissionCompletionRepository::class.java)
    private val fileUploadService = mock(FileUploadService::class.java)
    private val missionVerificationService = MissionVerificationService(
        userRepository = userRepository,
        missionRepository = missionRepository,
        missionCompletionRepository = missionCompletionRepository,
        fileUploadService = fileUploadService,
    )

    @Test
    fun `오늘 처음 제출하면 검수 대기 상태로 저장한다`() {
        val user = createUser()
        val mission = createMission()
        val (start, end) = todayRange()
        `when`(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user))
        `when`(missionRepository.findById(1L)).thenReturn(Optional.of(mission))
        `when`(
            missionCompletionRepository.findTopByUserIdAndMissionIdAndSubmittedAtBetweenOrderBySubmittedAtDesc(
                1L,
                1L,
                start,
                end,
            ),
        ).thenReturn(null)
        `when`(missionCompletionRepository.save(any(com.zerost.api.mission.domain.MissionCompletion::class.java))).thenAnswer { invocation ->
            val saved = invocation.arguments[0] as com.zerost.api.mission.domain.MissionCompletion
            createMissionCompletion(
                id = 55L,
                user = saved.user,
                mission = saved.mission,
                photoKey = saved.photoKey,
                status = saved.status,
                submittedAt = saved.submittedAt,
                reviewedAt = saved.reviewedAt,
            )
        }

        val response = missionVerificationService.submitVerification(
            userId = 1L,
            missionId = 1L,
            photoKey = "missions/1/1/2026/07/18/mission-1.jpg",
        )

        assertEquals(55L, response.completionId)
        assertEquals("PENDING", response.status)
    }

    @Test
    fun `오늘 이미 검수중이면 재제출할 수 없다`() {
        val user = createUser()
        val mission = createMission()
        val completion = createMissionCompletion(status = MissionCompletionStatus.PENDING)
        val (start, end) = todayRange()
        `when`(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user))
        `when`(missionRepository.findById(1L)).thenReturn(Optional.of(mission))
        `when`(
            missionCompletionRepository.findTopByUserIdAndMissionIdAndSubmittedAtBetweenOrderBySubmittedAtDesc(
                1L,
                1L,
                start,
                end,
            ),
        ).thenReturn(completion)

        val exception = assertThrows<BusinessException> {
            missionVerificationService.submitVerification(1L, 1L, "missions/1/1/2026/07/18/mission-1.jpg")
        }

        assertEquals(ErrorCode.MISSION_UNDER_REVIEW, exception.errorCode)
        verify(missionCompletionRepository, never()).save(any(com.zerost.api.mission.domain.MissionCompletion::class.java))
    }

    @Test
    fun `오늘 이미 승인된 미션이면 재제출할 수 없다`() {
        val user = createUser()
        val mission = createMission()
        val completion = createMissionCompletion(status = MissionCompletionStatus.APPROVED)
        val (start, end) = todayRange()
        `when`(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user))
        `when`(missionRepository.findById(1L)).thenReturn(Optional.of(mission))
        `when`(
            missionCompletionRepository.findTopByUserIdAndMissionIdAndSubmittedAtBetweenOrderBySubmittedAtDesc(
                1L,
                1L,
                start,
                end,
            ),
        ).thenReturn(completion)

        val exception = assertThrows<BusinessException> {
            missionVerificationService.submitVerification(1L, 1L, "missions/1/1/2026/07/18/mission-1.jpg")
        }

        assertEquals(ErrorCode.MISSION_ALREADY_COMPLETED, exception.errorCode)
    }

    @Test
    fun `검수 대기 상태의 미션 인증 이미지를 수정할 수 있다`() {
        val user = createUser(id = 1L)
        val mission = createMission(id = 3L)
        val completion = createMissionCompletion(
            id = 55L,
            user = user,
            mission = mission,
            photoKey = "missions/1/3/2026/07/18/old.jpg",
            status = MissionCompletionStatus.PENDING,
        )

        `when`(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user))
        `when`(missionCompletionRepository.findByIdForUpdate(55L)).thenReturn(Optional.of(completion))

        val response = missionVerificationService.updateVerification(
            userId = 1L,
            completionId = 55L,
            photoKey = "missions/1/3/2026/07/18/new.jpg",
        )

        assertEquals(55L, response.completionId)
        assertEquals("PENDING", response.status)
        assertEquals("missions/1/3/2026/07/18/new.jpg", response.photoKey)
        verify(fileUploadService).validateMissionImageKey(1L, 3L, "missions/1/3/2026/07/18/new.jpg")
        verify(fileUploadService).delete("missions/1/3/2026/07/18/old.jpg")
    }

    @Test
    fun `같은 photoKey로 수정 요청하면 기존 파일을 삭제하지 않는다`() {
        val user = createUser(id = 1L)
        val mission = createMission(id = 3L)
        val completion = createMissionCompletion(
            id = 55L,
            user = user,
            mission = mission,
            photoKey = "missions/1/3/2026/07/18/same.jpg",
            status = MissionCompletionStatus.REJECTED,
            reviewedAt = LocalDateTime.of(2026, 7, 18, 12, 0, 0),
        )

        `when`(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user))
        `when`(missionCompletionRepository.findByIdForUpdate(55L)).thenReturn(Optional.of(completion))

        val response = missionVerificationService.updateVerification(
            userId = 1L,
            completionId = 55L,
            photoKey = "missions/1/3/2026/07/18/same.jpg",
        )

        assertEquals("REJECTED", response.status)
        assertEquals(LocalDateTime.of(2026, 7, 18, 12, 0, 0), completion.reviewedAt)
        verify(fileUploadService).validateMissionImageKey(1L, 3L, "missions/1/3/2026/07/18/same.jpg")
        verify(fileUploadService, never()).delete("missions/1/3/2026/07/18/same.jpg")
    }

    @Test
    fun `반려된 미션 인증을 새 이미지로 수정하면 재검수 상태로 돌아간다`() {
        val user = createUser(id = 1L)
        val mission = createMission(id = 3L)
        val reviewedAt = LocalDateTime.of(2026, 7, 18, 12, 0, 0)
        val completion = createMissionCompletion(
            id = 55L,
            user = user,
            mission = mission,
            photoKey = "missions/1/3/2026/07/18/rejected-old.jpg",
            status = MissionCompletionStatus.REJECTED,
            reviewedAt = reviewedAt,
        )

        `when`(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user))
        `when`(missionCompletionRepository.findByIdForUpdate(55L)).thenReturn(Optional.of(completion))

        val response = missionVerificationService.updateVerification(
            userId = 1L,
            completionId = 55L,
            photoKey = "missions/1/3/2026/07/18/rejected-new.jpg",
        )

        assertEquals("PENDING", response.status)
        assertEquals(MissionCompletionStatus.PENDING, completion.status)
        assertEquals(null, completion.reviewedAt)
        verify(fileUploadService).validateMissionImageKey(1L, 3L, "missions/1/3/2026/07/18/rejected-new.jpg")
        verify(fileUploadService).delete("missions/1/3/2026/07/18/rejected-old.jpg")
    }

    @Test
    fun `승인된 미션 인증은 수정할 수 없다`() {
        val user = createUser(id = 1L)
        val mission = createMission(id = 3L)
        val completion = createMissionCompletion(
            id = 55L,
            user = user,
            mission = mission,
            status = MissionCompletionStatus.APPROVED,
        )

        `when`(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user))
        `when`(missionCompletionRepository.findByIdForUpdate(55L)).thenReturn(Optional.of(completion))

        val exception = assertThrows<BusinessException> {
            missionVerificationService.updateVerification(1L, 55L, "missions/1/3/2026/07/18/new.jpg")
        }

        assertEquals(ErrorCode.MISSION_COMPLETION_MODIFICATION_NOT_ALLOWED, exception.errorCode)
        verify(fileUploadService, never()).validateMissionImageKey(anyLong(), anyLong(), anyString())
        verify(fileUploadService, never()).delete(anyString())
    }

    @Test
    fun `내 미션 인증을 삭제할 수 있다`() {
        val user = createUser(id = 1L)
        val mission = createMission(id = 3L)
        val completion = createMissionCompletion(
            id = 55L,
            user = user,
            mission = mission,
            photoKey = "missions/1/3/2026/07/18/delete.jpg",
            status = MissionCompletionStatus.REJECTED,
        )

        `when`(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user))
        `when`(missionCompletionRepository.findByIdForUpdate(55L)).thenReturn(Optional.of(completion))

        val response = missionVerificationService.deleteVerification(
            userId = 1L,
            completionId = 55L,
        )

        assertEquals(55L, response.completionId)
        verify(missionCompletionRepository).delete(completion)
        verify(fileUploadService).delete("missions/1/3/2026/07/18/delete.jpg")
    }

    @Test
    fun `다른 유저의 미션 인증은 수정할 수 없다`() {
        val user = createUser(id = 1L)
        val completion = createMissionCompletion(
            id = 55L,
            user = createUser(id = 2L),
        )

        `when`(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user))
        `when`(missionCompletionRepository.findByIdForUpdate(55L)).thenReturn(Optional.of(completion))

        val exception = assertThrows<BusinessException> {
            missionVerificationService.updateVerification(1L, 55L, "missions/1/1/2026/07/18/new.jpg")
        }

        assertEquals(ErrorCode.MISSION_COMPLETION_NOT_FOUND, exception.errorCode)
        verify(fileUploadService, never()).validateMissionImageKey(anyLong(), anyLong(), anyString())
    }

    private fun todayRange(): Pair<LocalDateTime, LocalDateTime> {
        val today = LocalDate.now()
        return today.atStartOfDay() to today.plusDays(1).atStartOfDay()
    }
}
