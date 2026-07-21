package com.zerost.api.shop.presentation

import com.zerost.api.auth.application.AuthTokenProvider
import com.zerost.api.common.device.DeviceIdInterceptor
import com.zerost.api.common.exception.GlobalExceptionHandler
import com.zerost.api.shop.application.ShopQueryService
import com.zerost.api.shop.presentation.dto.ShopResponse
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

class ShopControllerTest {

    private val shopQueryService = mock(ShopQueryService::class.java)
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(ShopController(shopQueryService))
            .setControllerAdvice(GlobalExceptionHandler())
            .addInterceptors(DeviceIdInterceptor(mock(AuthTokenProvider::class.java)))
            .build()
    }

    @Test
    fun `디바이스 아이디가 있으면 상점 목록을 조회할 수 있다`() {
        `when`(shopQueryService.getShops()).thenReturn(
            listOf(
                ShopResponse(
                    id = 1L,
                    name = "알맹상점",
                    description = "제로웨이스트 샵",
                    imageUrl = "image-1",
                ),
            ),
        )

        mockMvc.perform(
            get("/api/v1/shops")
                .header("X-Device-Id", "device-1"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].id").value(1))
            .andExpect(jsonPath("$.data[0].name").value("알맹상점"))

        verify(shopQueryService).getShops()
    }

    @Test
    fun `디바이스 아이디가 없으면 상점 목록 조회에 실패한다`() {
        mockMvc.perform(get("/api/v1/shops"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("DEVICE_ID_HEADER_MISSING"))
    }
}
