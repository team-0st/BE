package com.zerost.api.soup.presentation

import com.zerost.api.common.device.DeviceIdInterceptor
import com.zerost.api.common.exception.GlobalExceptionHandler
import com.zerost.api.soup.application.SoupBrewingService
import com.zerost.api.soup.presentation.dto.BrewSoupResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean

class SoupControllerTest {

    private val soupBrewingService = mock(SoupBrewingService::class.java)
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        val validator = LocalValidatorFactoryBean().apply { afterPropertiesSet() }
        mockMvc = MockMvcBuilders.standaloneSetup(SoupController(soupBrewingService))
            .setControllerAdvice(GlobalExceptionHandler())
            .setValidator(validator)
            .addInterceptors(DeviceIdInterceptor())
            .build()
    }

    @Test
    fun `올바른 요청이면 스프를 제작할 수 있다`() {
        `when`(soupBrewingService.brew("device-1", listOf(1L, 2L, 3L))).thenReturn(
            BrewSoupResponse(
                soupId = 10L,
                recipeId = 1L,
                recipeName = "오리지널 스프",
                recipeType = "COMMON",
                rewardGrade = "JACKPOT",
                rewardEcoJam = 0,
                rewardAlmangPoint = 2_000,
                rewardedIngredients = emptyList(),
            ),
        )

        mockMvc.perform(
            post("/api/v1/soups/brew")
                .header("X-Device-Id", "device-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"ingredientIds":[1,2,3]}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.recipeName").value("오리지널 스프"))
            .andExpect(jsonPath("$.data.rewardGrade").value("JACKPOT"))

        verify(soupBrewingService).brew("device-1", listOf(1L, 2L, 3L))
    }

    @Test
    fun `재료 수가 부족하면 제작 요청에 실패한다`() {
        mockMvc.perform(
            post("/api/v1/soups/brew")
                .header("X-Device-Id", "device-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"ingredientIds":[1,2]}"""),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("INVALID_INPUT_VALUE"))
    }
}
