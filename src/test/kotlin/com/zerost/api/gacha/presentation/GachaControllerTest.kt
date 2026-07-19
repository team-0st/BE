package com.zerost.api.gacha.presentation

import com.zerost.api.common.device.DeviceIdInterceptor
import com.zerost.api.common.exception.GlobalExceptionHandler
import com.zerost.api.gacha.application.GachaExecutionService
import com.zerost.api.gacha.presentation.dto.ExecuteGachaResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class GachaControllerTest {

    private val gachaExecutionService = mock(GachaExecutionService::class.java)
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(GachaController(gachaExecutionService))
            .setControllerAdvice(GlobalExceptionHandler())
            .addInterceptors(DeviceIdInterceptor())
            .build()
    }

    @Test
    fun `디바이스 아이디가 있으면 가챠를 실행할 수 있다`() {
        `when`(gachaExecutionService.execute("device-1")).thenReturn(
            ExecuteGachaResponse(
                gachaId = 10L,
                costEcoJam = 100,
                remainingEcoJam = 200,
                resultType = "POINT",
                resultPoint = 300,
                resultEcoJam = 0,
                resultIngredientId = null,
                resultIngredientQuantity = 0,
            ),
        )

        mockMvc.perform(
            post("/api/v1/gachas/draw")
                .header("X-Device-Id", "device-1"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.gachaId").value(10))
            .andExpect(jsonPath("$.data.remainingEcoJam").value(200))
            .andExpect(jsonPath("$.data.resultType").value("POINT"))

        verify(gachaExecutionService).execute("device-1")
    }
}
