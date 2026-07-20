package com.zerost.api.recipe.presentation

import com.zerost.api.common.device.DeviceIdInterceptor
import com.zerost.api.common.exception.GlobalExceptionHandler
import com.zerost.api.recipe.application.RecipeQueryService
import com.zerost.api.recipe.presentation.dto.RecipeDetailIngredientResponse
import com.zerost.api.recipe.presentation.dto.RecipeDetailResponse
import com.zerost.api.recipe.presentation.dto.RecipeSummaryResponse
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

class RecipeControllerTest {

    private val recipeQueryService = mock(RecipeQueryService::class.java)
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(RecipeController(recipeQueryService))
            .setControllerAdvice(GlobalExceptionHandler())
            .addInterceptors(DeviceIdInterceptor())
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
                .header("X-Device-Id", "device-1"),
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
                .header("X-Device-Id", "device-1"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.recipeId").value(1))
            .andExpect(jsonPath("$.data.ingredients[0].name").value("양배추"))

        verify(recipeQueryService).getRecipe("device-1", 1L)
    }
}
