package com.zerost.api.shop.presentation

import com.zerost.api.common.response.ApiResponse
import com.zerost.api.shop.application.ShopQueryService
import com.zerost.api.shop.presentation.dto.ShopResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Shop", description = "샵 조회 API")
@RestController
@RequestMapping("/api/v1/shops")
class ShopController(
    private val shopQueryService: ShopQueryService
) {

    @Operation(
        summary = "샵 목록 조회",
        description = "온보딩에 필요한 샵 목록을 조회합니다.",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "샵 목록 조회 성공"),
            SwaggerApiResponse(responseCode = "400", description = "잘못된 요청"),
        ],
    )
    @GetMapping
    fun getShops(): ApiResponse<List<ShopResponse>> {
        val response = shopQueryService.getShops()
        return ApiResponse.success(response)
    }
}
