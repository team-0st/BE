package com.zerost.api.ingredient.presentation

import com.zerost.api.common.device.DeviceConstants
import com.zerost.api.common.response.ApiResponse
import com.zerost.api.ingredient.application.UserIngredientQueryService
import com.zerost.api.ingredient.presentation.dto.UserIngredientResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Ingredient", description = "재료 조회 API")
@RestController
@RequestMapping("/api/v1/ingredients")
class IngredientController(
    private val userIngredientQueryService: UserIngredientQueryService,
) {

    @Operation(
        summary = "보유 재료 목록 조회",
        description = "현재 유저가 보유한 재료 목록과 수량을 조회합니다.",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "조회 성공"),
            SwaggerApiResponse(responseCode = "404", description = "등록된 유저를 찾을 수 없음"),
        ],
    )
    @GetMapping
    fun getUserIngredients(
        httpServletRequest: HttpServletRequest,
    ): ApiResponse<List<UserIngredientResponse>> {
        val deviceId = httpServletRequest.getAttribute(DeviceConstants.DEVICE_ID_ATTRIBUTE) as String
        val response = userIngredientQueryService.getUserIngredients(deviceId)
        return ApiResponse.success(response)
    }
}
