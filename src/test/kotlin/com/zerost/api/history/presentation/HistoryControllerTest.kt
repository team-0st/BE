package com.zerost.api.history.presentation

import com.zerost.api.common.device.DeviceIdInterceptor
import com.zerost.api.common.exception.GlobalExceptionHandler
import com.zerost.api.history.application.HistoryQueryService
import com.zerost.api.history.presentation.dto.AssetHistoryResponse
import com.zerost.api.support.createAuthTokenProvider
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class HistoryControllerTest {

    private val historyQueryService = mock(HistoryQueryService::class.java)
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(HistoryController(historyQueryService))
            .setControllerAdvice(GlobalExceptionHandler())
            .addInterceptors(DeviceIdInterceptor(createAuthTokenProvider()))
            .build()
    }

    @Test
    fun `에코잼 적립 내역을 조회할 수 있다`() {
        `when`(historyQueryService.getEcoJamHistories("device-1")).thenReturn(
            listOf(
                AssetHistoryResponse(
                    historyId = 1L,
                    amount = 300,
                    sourceType = "SOUP",
                    sourceId = 10L,
                    createdAt = "2026-07-19T22:30:00",
                ),
            ),
        )

        mockMvc.perform(
            get("/api/v1/histories/eco-jams")
                .header("Authorization", "Bearer access-token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].amount").value(300))
            .andExpect(jsonPath("$.data[0].sourceType").value("SOUP"))

        verify(historyQueryService).getEcoJamHistories("device-1")
    }

    @Test
    fun `포인트 적립 내역을 조회할 수 있다`() {
        `when`(historyQueryService.getPointHistories("device-1")).thenReturn(
            listOf(
                AssetHistoryResponse(
                    historyId = 2L,
                    amount = 2_000,
                    sourceType = "SOUP",
                    sourceId = 20L,
                    createdAt = "2026-07-19T22:31:00",
                ),
            ),
        )

        mockMvc.perform(
            get("/api/v1/histories/points")
                .header("Authorization", "Bearer access-token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].amount").value(2000))
            .andExpect(jsonPath("$.data[0].sourceId").value(20))

        verify(historyQueryService).getPointHistories("device-1")
    }
}
