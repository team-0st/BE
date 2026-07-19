package com.zerost.api.history.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.ecojam.domain.EcoJamHistoryRepository
import com.zerost.api.point.domain.PointHistoryRepository
import com.zerost.api.support.createEcoJamHistory
import com.zerost.api.support.createPointHistory
import com.zerost.api.support.createUser
import com.zerost.api.user.domain.UserRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.Optional
import kotlin.test.assertEquals

class HistoryQueryServiceTest {

    private val userRepository = mock(UserRepository::class.java)
    private val ecoJamHistoryRepository = mock(EcoJamHistoryRepository::class.java)
    private val pointHistoryRepository = mock(PointHistoryRepository::class.java)

    private val historyQueryService = HistoryQueryService(
        userRepository = userRepository,
        ecoJamHistoryRepository = ecoJamHistoryRepository,
        pointHistoryRepository = pointHistoryRepository,
    )

    @Test
    fun `에코잼 적립 내역을 최신순으로 조회할 수 있다`() {
        val user = createUser(id = 1L, deviceId = "device-1")
        val latestHistory = createEcoJamHistory(id = 2L, user = user, amount = 500, sourceId = 20L)
        val olderHistory = createEcoJamHistory(
            id = 1L,
            user = user,
            amount = 300,
            sourceId = 10L,
            createdAt = latestHistory.createdAt!!.minusDays(1),
        )

        `when`(userRepository.findByDeviceId("device-1")).thenReturn(Optional.of(user))
        `when`(ecoJamHistoryRepository.findAllByUserIdOrderByCreatedAtDesc(1L))
            .thenReturn(listOf(latestHistory, olderHistory))

        val response = historyQueryService.getEcoJamHistories("device-1")

        assertEquals(2, response.size)
        assertEquals(2L, response[0].historyId)
        assertEquals(500, response[0].amount)
        assertEquals("SOUP", response[0].sourceType)
        assertEquals(20L, response[0].sourceId)
    }

    @Test
    fun `포인트 적립 내역을 최신순으로 조회할 수 있다`() {
        val user = createUser(id = 1L, deviceId = "device-1")
        val pointHistory = createPointHistory(id = 3L, user = user, amount = 2_000, sourceId = 30L)

        `when`(userRepository.findByDeviceId("device-1")).thenReturn(Optional.of(user))
        `when`(pointHistoryRepository.findAllByUserIdOrderByCreatedAtDesc(1L))
            .thenReturn(listOf(pointHistory))

        val response = historyQueryService.getPointHistories("device-1")

        assertEquals(1, response.size)
        assertEquals(3L, response[0].historyId)
        assertEquals(2_000, response[0].amount)
        assertEquals("SOUP", response[0].sourceType)
    }

    @Test
    fun `등록되지 않은 디바이스면 적립 내역 조회에 실패한다`() {
        `when`(userRepository.findByDeviceId("device-1")).thenReturn(Optional.empty())

        val exception = assertThrows<BusinessException> {
            historyQueryService.getEcoJamHistories("device-1")
        }

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.errorCode)
    }
}
