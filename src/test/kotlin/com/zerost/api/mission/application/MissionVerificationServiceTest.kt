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
        `when`(userRepository.findByDeviceIdForUpdate("device-1")).thenReturn(Optional.of(user))
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
            deviceId = "device-1",
            missionId = 1L,
            photoKey = "missions/device-1/1/2026/07/18/mission-1.jpg",
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
        `when`(userRepository.findByDeviceIdForUpdate("device-1")).thenReturn(Optional.of(user))
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
            missionVerificationService.submitVerification("device-1", 1L, "missions/device-1/1/2026/07/18/mission-1.jpg")
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
        `when`(userRepository.findByDeviceIdForUpdate("device-1")).thenReturn(Optional.of(user))
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
            missionVerificationService.submitVerification("device-1", 1L, "missions/device-1/1/2026/07/18/mission-1.jpg")
        }

        assertEquals(ErrorCode.MISSION_ALREADY_COMPLETED, exception.errorCode)
    }

    private fun todayRange(): Pair<LocalDateTime, LocalDateTime> {
        val today = LocalDate.now()
        return today.atStartOfDay() to today.plusDays(1).atStartOfDay()
    }
}
