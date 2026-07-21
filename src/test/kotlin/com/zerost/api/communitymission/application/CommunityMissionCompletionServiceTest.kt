package com.zerost.api.communitymission.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.communitymission.domain.CommunityMissionCompletion
import com.zerost.api.communitymission.domain.CommunityMissionCompletionRepository
import com.zerost.api.communitymission.domain.CommunityMissionRepository
import com.zerost.api.communitymission.domain.CommunityMissionDifficulty
import com.zerost.api.support.createCommunityMission
import com.zerost.api.support.createUser
import com.zerost.api.user.domain.UserRepository
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CommunityMissionCompletionServiceTest {

    private val userRepository = mock(UserRepository::class.java)
    private val communityMissionRepository = mock(CommunityMissionRepository::class.java)
    private val communityMissionCompletionRepository = mock(CommunityMissionCompletionRepository::class.java)
    private val communityMissionCompletionService = CommunityMissionCompletionService(
        userRepository = userRepository,
        communityMissionRepository = communityMissionRepository,
        communityMissionCompletionRepository = communityMissionCompletionRepository,
    )

    @Test
    fun `해금된 공동 미션은 완료 처리할 수 있다`() {
        val user = createUser(onboardingCompleted = true)
        val mission1 = createCommunityMission(
            id = 1L,
            difficulty = CommunityMissionDifficulty.ONE_STAR,
            stage = 1,
        )
        val mission2 = createCommunityMission(
            id = 2L,
            difficulty = CommunityMissionDifficulty.ONE_STAR,
            stage = 2,
        )

        `when`(userRepository.findByDeviceIdForUpdate("device-1")).thenReturn(Optional.of(user))
        `when`(communityMissionRepository.findAllByActiveTrue()).thenReturn(listOf(mission2, mission1))
        `when`(communityMissionCompletionRepository.findCompletedMissionIdsByUserId(1L)).thenReturn(listOf(1L))
        `when`(communityMissionCompletionRepository.existsByCommunityMissionIdAndUserId(2L, 1L)).thenReturn(false)
        `when`(communityMissionCompletionRepository.save(any(CommunityMissionCompletion::class.java))).thenAnswer { invocation ->
            val completion = invocation.arguments[0] as CommunityMissionCompletion
            CommunityMissionCompletion(
                id = 10L,
                communityMission = completion.communityMission,
                user = completion.user,
                completedAt = completion.completedAt,
            )
        }

        val response = communityMissionCompletionService.complete("device-1", 2L)

        assertEquals(10L, response.completionId)
        assertEquals(2L, response.communityMissionId)
        val captor = ArgumentCaptor.forClass(CommunityMissionCompletion::class.java)
        verify(communityMissionCompletionRepository).save(captor.capture())
        assertEquals(2L, captor.value.communityMission.id)
        assertEquals(1L, captor.value.user.id)
    }

    @Test
    fun `온보딩을 완료하지 않으면 공동 미션을 완료할 수 없다`() {
        val user = createUser(onboardingCompleted = false)

        `when`(userRepository.findByDeviceIdForUpdate("device-1")).thenReturn(Optional.of(user))

        val exception = assertFailsWith<BusinessException> {
            communityMissionCompletionService.complete("device-1", 1L)
        }

        assertEquals(ErrorCode.COMMUNITY_MISSION_ONBOARDING_REQUIRED, exception.errorCode)
        verify(communityMissionRepository, never()).findAllByActiveTrue()
    }

    @Test
    fun `이전 단계를 완료하지 않으면 잠긴 공동 미션을 완료할 수 없다`() {
        val user = createUser(onboardingCompleted = true)
        val mission1 = createCommunityMission(
            id = 1L,
            difficulty = CommunityMissionDifficulty.TWO_STAR,
            stage = 1,
        )
        val mission2 = createCommunityMission(
            id = 2L,
            difficulty = CommunityMissionDifficulty.TWO_STAR,
            stage = 2,
        )

        `when`(userRepository.findByDeviceIdForUpdate("device-1")).thenReturn(Optional.of(user))
        `when`(communityMissionRepository.findAllByActiveTrue()).thenReturn(listOf(mission1, mission2))
        `when`(communityMissionCompletionRepository.findCompletedMissionIdsByUserId(1L)).thenReturn(emptyList())

        val exception = assertFailsWith<BusinessException> {
            communityMissionCompletionService.complete("device-1", 2L)
        }

        assertEquals(ErrorCode.COMMUNITY_MISSION_NOT_UNLOCKED, exception.errorCode)
    }

    @Test
    fun `이미 완료한 공동 미션은 다시 완료할 수 없다`() {
        val user = createUser(onboardingCompleted = true)
        val mission = createCommunityMission(id = 1L)

        `when`(userRepository.findByDeviceIdForUpdate("device-1")).thenReturn(Optional.of(user))
        `when`(communityMissionRepository.findAllByActiveTrue()).thenReturn(listOf(mission))
        `when`(communityMissionCompletionRepository.findCompletedMissionIdsByUserId(1L)).thenReturn(listOf(1L))
        `when`(communityMissionCompletionRepository.existsByCommunityMissionIdAndUserId(1L, 1L)).thenReturn(true)

        val exception = assertFailsWith<BusinessException> {
            communityMissionCompletionService.complete("device-1", 1L)
        }

        assertEquals(ErrorCode.COMMUNITY_MISSION_ALREADY_COMPLETED, exception.errorCode)
    }
}
