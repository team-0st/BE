package com.zerost.api.recipe.presentation

import com.zerost.api.common.device.DeviceConstants
import com.zerost.api.common.response.ApiResponse
import com.zerost.api.recipe.application.RecipeQueryService
import com.zerost.api.recipe.presentation.dto.RecipeDetailResponse
import com.zerost.api.recipe.presentation.dto.RecipeSummaryResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Recipe", description = "레시피 조회 API")
@RestController
@RequestMapping("/api/v1/recipes")
class RecipeController(
    private val recipeQueryService: RecipeQueryService,
) {

    @Operation(
        summary = "레시피 목록 조회",
        description = "현재 서비스에 등록된 레시피 목록을 조회합니다. 비공개 레시피는 이름과 재료 조합이 마스킹됩니다.",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "레시피 목록 조회 성공"),
            SwaggerApiResponse(responseCode = "404", description = "등록된 유저를 찾을 수 없음"),
        ],
    )
    @GetMapping
    fun getRecipes(
        httpServletRequest: HttpServletRequest,
    ): ApiResponse<List<RecipeSummaryResponse>> {
        val deviceId = httpServletRequest.getAttribute(DeviceConstants.DEVICE_ID_ATTRIBUTE) as String
        val response = recipeQueryService.getRecipes(deviceId)
        return ApiResponse.success(response)
    }

    @Operation(
        summary = "레시피 상세 조회",
        description = "선택한 레시피의 상세 정보를 조회합니다. 비공개 레시피는 재료 조합을 반환하지 않습니다.",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "레시피 상세 조회 성공"),
            SwaggerApiResponse(responseCode = "404", description = "등록된 유저 또는 레시피를 찾을 수 없음"),
        ],
    )
    @GetMapping("/{recipeId}")
    fun getRecipe(
        @PathVariable recipeId: Long,
        httpServletRequest: HttpServletRequest,
    ): ApiResponse<RecipeDetailResponse> {
        val deviceId = httpServletRequest.getAttribute(DeviceConstants.DEVICE_ID_ATTRIBUTE) as String
        val response = recipeQueryService.getRecipe(deviceId, recipeId)
        return ApiResponse.success(response)
    }
}
