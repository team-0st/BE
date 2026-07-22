package com.zerost.api.recipe.presentation

import com.zerost.api.common.device.DeviceIdInterceptor
import com.zerost.api.common.exception.GlobalExceptionHandler
import com.zerost.api.recipe.application.RecipeQueryService
import com.zerost.api.recipe.application.RecipeUnlockService
import com.zerost.api.recipe.presentation.dto.RecipeDetailIngredientResponse
import com.zerost.api.recipe.presentation.dto.RecipeDetailResponse
import com.zerost.api.recipe.presentation.dto.RecipeSectionsResponse
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
    fun `인증된 사용자는 레시피 목록을 조회할 수 있다`() {
        `when`(recipeQueryService.getRecipes(1L)).thenReturn(
            RecipeSectionsResponse(
                introRecipes = listOf(
                    RecipeSummaryResponse(
                        recipeId = 1L,
                        name = "따뜻한 입문 스프",
                        type = "COMMON",
                        slotCount = 2,
                        recipeVisible = true,
                        hints = emptyList(),
                    ),
                ),
                weeklyRecipe = RecipeSummaryResponse(
                    recipeId = 2L,
                    name = "오리지널 스프",
                    type = "COMMON",
                    slotCount = 3,
                    recipeVisible = true,
                    hints = emptyList(),
                ),
                hiddenRecipes = listOf(
                    RecipeSummaryResponse(
                        recipeId = 3L,
                        name = "???",
                        type = "HIDDEN",
                        slotCount = 4,
                        recipeVisible = false,
                        hints = emptyList(),
                    ),
                ),
            ),
        )

        mockMvc.perform(
            get("/api/v1/recipes")
                .header("Authorization", "Bearer access-token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.introRecipes[0].name").value("따뜻한 입문 스프"))
            .andExpect(jsonPath("$.data.weeklyRecipe.name").value("오리지널 스프"))
            .andExpect(jsonPath("$.data.hiddenRecipes[0].name").value("???"))
            .andExpect(jsonPath("$.data.hiddenRecipes[0].recipeVisible").value(false))

        verify(recipeQueryService).getRecipes(1L)
    }

    @Test
    fun `인증된 사용자는 레시피 상세를 조회할 수 있다`() {
        `when`(recipeQueryService.getRecipe(1L, 1L)).thenReturn(
            RecipeDetailResponse(
                recipeId = 1L,
                name = "오리지널 스프",
                type = "COMMON",
                slotCount = 3,
                recipeVisible = true,
                hints = emptyList(),
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

        verify(recipeQueryService).getRecipe(1L, 1L)
    }

    @Test
    fun `인증된 사용자는 희귀 레시피를 랜덤 해금할 수 있다`() {
        `when`(recipeUnlockService.unlockRandomHiddenRecipe(1L)).thenReturn(
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

        verify(recipeUnlockService).unlockRandomHiddenRecipe(1L)
    }
}
