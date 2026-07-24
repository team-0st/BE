package com.zerost.api.point.application

import com.zerost.api.point.domain.PointHistoryRepository
import com.zerost.api.point.domain.PointHistorySourceType
import com.zerost.api.support.createUser
import com.zerost.api.user.domain.UserRepository
import java.util.Optional
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import kotlin.test.assertEquals

class PointAwardServiceTest {

    private val userRepository = mock(UserRepository::class.java)
    private val pointHistoryRepository = mock(PointHistoryRepository::class.java)

    @Test
    fun `상한 설정이 없으면 요청한 포인트를 그대로 지급한다`() {
        val user = createUser(id = 1L, point = 0)
        `when`(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user))
        val service = PointAwardService(
            userRepository = userRepository,
            pointHistoryRepository = pointHistoryRepository,
            pointPolicyProperties = PointPolicyProperties(maxCumulativeEarnAmountPerUser = null),
        )

        val grantedAmount = service.award(
            user = user,
            requestedAmount = 500,
            sourceType = PointHistorySourceType.GACHA,
            sourceId = 10L,
        )

        assertEquals(500, grantedAmount)
        assertEquals(500, user.point)
        verify(userRepository).findByIdForUpdate(1L)
        verify(pointHistoryRepository).save(any())
    }

    @Test
    fun `상한이 남아 있으면 남은 한도만큼만 부분 지급한다`() {
        val user = createUser(id = 1L, point = 0)
        `when`(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user))
        val service = PointAwardService(
            userRepository = userRepository,
            pointHistoryRepository = pointHistoryRepository,
            pointPolicyProperties = PointPolicyProperties(maxCumulativeEarnAmountPerUser = "5000"),
        )
        `when`(pointHistoryRepository.sumEarnedAmountByUserId(1L)).thenReturn(4_900L)

        val grantedAmount = service.award(
            user = user,
            requestedAmount = 300,
            sourceType = PointHistorySourceType.SOUP,
            sourceId = 20L,
        )

        assertEquals(100, grantedAmount)
        assertEquals(100, user.point)
        verify(userRepository).findByIdForUpdate(1L)
        verify(pointHistoryRepository).save(any())
    }

    @Test
    fun `상한을 모두 소진했으면 포인트를 지급하지 않는다`() {
        val user = createUser(id = 1L, point = 0)
        `when`(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user))
        val service = PointAwardService(
            userRepository = userRepository,
            pointHistoryRepository = pointHistoryRepository,
            pointPolicyProperties = PointPolicyProperties(maxCumulativeEarnAmountPerUser = "5000"),
        )
        `when`(pointHistoryRepository.sumEarnedAmountByUserId(1L)).thenReturn(5_000L)

        val grantedAmount = service.award(
            user = user,
            requestedAmount = 300,
            sourceType = PointHistorySourceType.SOUP_REROLL,
            sourceId = 30L,
        )

        assertEquals(0, grantedAmount)
        assertEquals(0, user.point)
        verify(userRepository).findByIdForUpdate(1L)
        verify(pointHistoryRepository, never()).save(any())
    }

    @Test
    fun `상한 설정값이 정수가 아니면 즉시 예외가 발생한다`() {
        assertThrows<IllegalArgumentException> {
            PointPolicyProperties(maxCumulativeEarnAmountPerUser = "abc")
        }
    }

    @Test
    fun `상한 설정값이 음수면 즉시 예외가 발생한다`() {
        assertThrows<IllegalArgumentException> {
            PointPolicyProperties(maxCumulativeEarnAmountPerUser = "-1")
        }
    }
}
