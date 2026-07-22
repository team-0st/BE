package com.zerost.api.mypage.presentation

import com.zerost.api.common.device.DeviceIdInterceptor
import com.zerost.api.common.exception.GlobalExceptionHandler
import com.zerost.api.mypage.application.MyPageQueryService
import com.zerost.api.mypage.presentation.dto.MyPageIngredientResponse
import com.zerost.api.mypage.presentation.dto.MyPageResponse
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

class MyPageControllerTest {

    private val myPageQueryService = mock(MyPageQueryService::class.java)
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(MyPageController(myPageQueryService))
            .setControllerAdvice(GlobalExceptionHandler())
            .addInterceptors(DeviceIdInterceptor(createAuthTokenProvider()))
            .build()
    }

    @Test
    fun `인증된 사용자는 마이페이지 통합 조회를 할 수 있다`() {
        `when`(myPageQueryService.getMyPage(1L)).thenReturn(
            MyPageResponse(
                nickname = "펭귄탐험가",
                profileCharacterCode = "CABBAGE",
                profileCharacterImageUrl = "https://assets.zero-st.com/profile-characters/cabbage.png",
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
                .header("Authorization", "Bearer access-token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.profileCharacterCode").value("CABBAGE"))
            .andExpect(jsonPath("$.data.profileCharacterImageUrl").value("https://assets.zero-st.com/profile-characters/cabbage.png"))
            .andExpect(jsonPath("$.data.shopName").value("알맹상점"))
            .andExpect(jsonPath("$.data.brewedSoupCount").value(4))
            .andExpect(jsonPath("$.data.ingredients[0].name").value("양배추"))

        verify(myPageQueryService).getMyPage(1L)
    }
}
