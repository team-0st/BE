package com.zerost.api.recipe.presentation

import com.zerost.api.common.device.DeviceIdInterceptor
import com.zerost.api.common.exception.GlobalExceptionHandler
import com.zerost.api.recipe.application.RecipeQueryService
import com.zerost.api.recipe.application.RecipeUnlockService
import com.zerost.api.recipe.presentation.dto.RecipeDetailIngredientResponse
import com.zerost.api.recipe.presentation.dto.RecipeDetailResponse
import com.zerost.api.recipe.presentation.dto.RecipeSummaryResponse
import com.zerost.api.recipe.presentation.dto.UnlockHiddenRecipeResponse
import com.zerost.api.support.createAuthTokenProvider
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class RecipeControllerTest {

    private val recipeQueryService = mock(RecipeQueryService::class.java)
    private val recipeUnlockService = mock(RecipeUnlockService::class.java)
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(RecipeController(recipeQueryService, recipeUnlockService))
            .setControllerAdvice(GlobalExceptionHandler())
            .addInterceptors(DeviceIdInterceptor(createAuthTokenProvider()))
            .build()
    }

    @Test
    fun `디바이스 아이디가 있으면 레시피 목록을 조회할 수 있다`() {
        `when`(recipeQueryService.getRecipes("device-1")).thenReturn(
            listOf(
                RecipeSummaryResponse(
                    recipeId = 1L,
                    name = "오리지널 스프",
                    type = "COMMON",
                    slotCount = 3,
                    recipeVisible = true,
                ),
                RecipeSummaryResponse(
                    recipeId = 2L,
                    name = "???",
                    type = "HIDDEN",
                    slotCount = 4,
                    recipeVisible = false,
                ),
            ),
        )

        mockMvc.perform(
            get("/api/v1/recipes")
                .header("Authorization", "Bearer access-token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].name").value("오리지널 스프"))
            .andExpect(jsonPath("$.data[1].name").value("???"))
            .andExpect(jsonPath("$.data[1].recipeVisible").value(false))

        verify(recipeQueryService).getRecipes("device-1")
    }

    @Test
    fun `디바이스 아이디가 있으면 레시피 상세를 조회할 수 있다`() {
        `when`(recipeQueryService.getRecipe("device-1", 1L)).thenReturn(
            RecipeDetailResponse(
                recipeId = 1L,
                name = "오리지널 스프",
                type = "COMMON",
                slotCount = 3,
                recipeVisible = true,
                ingredients = listOf(
                    RecipeDetailIngredientResponse(
                        ingredientId = 1L,
                        name = "양배추",
                        type = "COMMON",
                        imageUrl = "image-1",
                        slotOrder = 1,
                    ),
                ),
            ),
        )

        mockMvc.perform(
            get("/api/v1/recipes/1")
                .header("Authorization", "Bearer access-token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.recipeId").value(1))
            .andExpect(jsonPath("$.data.ingredients[0].name").value("양배추"))

        verify(recipeQueryService).getRecipe("device-1", 1L)
    }

    @Test
    fun `디바이스 아이디가 있으면 희귀 레시피를 랜덤 해금할 수 있다`() {
        `when`(recipeUnlockService.unlockRandomHiddenRecipe("device-1")).thenReturn(
            UnlockHiddenRecipeResponse(
                recipeId = 2L,
                recipeName = "크리스탈 스프",
                remainingEcoJam = 300,
            ),
        )

        mockMvc.perform(
            post("/api/v1/recipes/unlock/hidden")
                .header("Authorization", "Bearer access-token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.recipeId").value(2))
            .andExpect(jsonPath("$.data.recipeName").value("크리스탈 스프"))
            .andExpect(jsonPath("$.data.remainingEcoJam").value(300))

        verify(recipeUnlockService).unlockRandomHiddenRecipe("device-1")
    }
}
