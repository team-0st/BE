package com.zerost.api.ingredient.presentation

import com.zerost.api.common.device.DeviceIdInterceptor
import com.zerost.api.common.exception.GlobalExceptionHandler
import com.zerost.api.ingredient.application.UserIngredientQueryService
import com.zerost.api.ingredient.presentation.dto.UserIngredientResponse
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

class IngredientControllerTest {

    private val userIngredientQueryService = mock(UserIngredientQueryService::class.java)
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(IngredientController(userIngredientQueryService))
            .setControllerAdvice(GlobalExceptionHandler())
            .addInterceptors(DeviceIdInterceptor(createAuthTokenProvider()))
            .build()
    }

    @Test
    fun `디바이스 아이디가 있으면 보유 재료 목록을 조회할 수 있다`() {
        `when`(userIngredientQueryService.getUserIngredients("device-1")).thenReturn(
            listOf(
                UserIngredientResponse(
                    ingredientId = 7L,
                    name = "낡은 밧줄",
                    type = "COMMON",
                    imageUrl = "image-7",
                    quantity = 3,
                ),
            ),
        )

        mockMvc.perform(
            get("/api/v1/ingredients")
                .header("Authorization", "Bearer access-token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].ingredientId").value(7))
            .andExpect(jsonPath("$.data[0].quantity").value(3))

        verify(userIngredientQueryService).getUserIngredients("device-1")
    }
}
