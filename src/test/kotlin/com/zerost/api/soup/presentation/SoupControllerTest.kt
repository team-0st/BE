package com.zerost.api.soup.presentation

import com.zerost.api.common.device.DeviceIdInterceptor
import com.zerost.api.common.exception.GlobalExceptionHandler
import com.zerost.api.soup.application.SoupBrewingService
import com.zerost.api.soup.application.SoupRerollService
import com.zerost.api.soup.presentation.dto.BrewSoupResponse
import com.zerost.api.soup.presentation.dto.RerollSoupResponse
import com.zerost.api.support.createAuthTokenProvider
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
    private val soupRerollService = mock(SoupRerollService::class.java)
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        val validator = LocalValidatorFactoryBean().apply { afterPropertiesSet() }
        mockMvc = MockMvcBuilders.standaloneSetup(SoupController(soupBrewingService, soupRerollService))
            .setControllerAdvice(GlobalExceptionHandler())
            .setValidator(validator)
            .addInterceptors(DeviceIdInterceptor(createAuthTokenProvider()))
            .build()
    }

    @Test
    fun `올바른 요청이면 스프를 제작할 수 있다`() {
        `when`(soupBrewingService.brew(1L, listOf(1L, 2L, 3L))).thenReturn(
            BrewSoupResponse(
                soupId = 10L,
                recipeId = 1L,
                recipeName = "오리지널 스프",
                recipeType = "COMMON",
                rewardGrade = "JACKPOT",
                rewardEcoJam = 0,
                rewardPoint = 2_000,
                rewardedIngredients = emptyList(),
            ),
        )

        mockMvc.perform(
            post("/api/v1/soups/brew")
                .header("Authorization", "Bearer access-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"ingredientIds":[1,2,3]}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.recipeName").value("오리지널 스프"))
            .andExpect(jsonPath("$.data.rewardGrade").value("JACKPOT"))

        verify(soupBrewingService).brew(1L, listOf(1L, 2L, 3L))
    }

    @Test
    fun `2개 재료로도 입문 스프 제작 요청을 보낼 수 있다`() {
        `when`(soupBrewingService.brew(1L, listOf(1L, 2L))).thenReturn(
            BrewSoupResponse(
                soupId = 11L,
                recipeId = 2L,
                recipeName = "따뜻한 입문 스프",
                recipeType = "COMMON",
                rewardGrade = "CONSOLATION",
                rewardEcoJam = 30,
                rewardPoint = 0,
                rewardedIngredients = emptyList(),
            ),
        )

        mockMvc.perform(
            post("/api/v1/soups/brew")
                .header("Authorization", "Bearer access-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"ingredientIds":[1,2]}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.recipeName").value("따뜻한 입문 스프"))

        verify(soupBrewingService).brew(1L, listOf(1L, 2L))
    }

    @Test
    fun `디바이스 아이디가 있으면 스프 보상을 리롤할 수 있다`() {
        `when`(soupRerollService.reroll(1L, 10L)).thenReturn(
            RerollSoupResponse(
                soupId = 10L,
                rerollCostEcoJam = 30,
                remainingEcoJam = 70,
                rewardGrade = "SMALL",
                rewardEcoJam = 0,
                rewardPoint = 500,
                rewardedIngredients = emptyList(),
            ),
        )

        mockMvc.perform(
            post("/api/v1/soups/10/reroll")
                .header("Authorization", "Bearer access-token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.soupId").value(10))
            .andExpect(jsonPath("$.data.rewardGrade").value("SMALL"))
            .andExpect(jsonPath("$.data.remainingEcoJam").value(70))

        verify(soupRerollService).reroll(1L, 10L)
    }

    @Test
    fun `재료 수가 2개 미만이면 제작 요청에 실패한다`() {
        mockMvc.perform(
            post("/api/v1/soups/brew")
                .header("Authorization", "Bearer access-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"ingredientIds":[1]}"""),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("INVALID_INPUT_VALUE"))
    }
}
