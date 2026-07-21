package com.zerost.api.mypage.presentation

import com.zerost.api.auth.application.AuthTokenProvider
import com.zerost.api.common.device.DeviceIdInterceptor
import com.zerost.api.common.exception.GlobalExceptionHandler
import com.zerost.api.mypage.application.MyPageQueryService
import com.zerost.api.mypage.presentation.dto.MyPageIngredientResponse
import com.zerost.api.mypage.presentation.dto.MyPageResponse
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

class MyPageControllerTest {

    private val myPageQueryService = mock(MyPageQueryService::class.java)
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(MyPageController(myPageQueryService))
            .setControllerAdvice(GlobalExceptionHandler())
            .addInterceptors(DeviceIdInterceptor(mock(AuthTokenProvider::class.java)))
            .build()
    }

    @Test
    fun `디바이스 아이디가 있으면 마이페이지 통합 조회를 할 수 있다`() {
        `when`(myPageQueryService.getMyPage("device-1")).thenReturn(
            MyPageResponse(
                nickname = "펭귄탐험가",
                shopName = "알맹상점",
                ecoJam = 410,
                point = 2200,
                brewedSoupCount = 4,
                completedMissionCount = 3,
                totalIngredientQuantity = 7,
                ingredients = listOf(
                    MyPageIngredientResponse(
                        ingredientId = 1L,
                        name = "양배추",
                        type = "COMMON",
                        imageUrl = "image-1",
                        quantity = 2,
                    ),
                ),
            ),
        )

        mockMvc.perform(
            get("/api/v1/my-page")
                .header("X-Device-Id", "device-1"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.shopName").value("알맹상점"))
            .andExpect(jsonPath("$.data.brewedSoupCount").value(4))
            .andExpect(jsonPath("$.data.ingredients[0].name").value("양배추"))

        verify(myPageQueryService).getMyPage("device-1")
    }
}
