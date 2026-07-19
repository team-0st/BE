package com.zerost.api.soup.presentation

import com.zerost.api.common.device.DeviceConstants
import com.zerost.api.common.response.ApiResponse
import com.zerost.api.soup.application.SoupBrewingService
import com.zerost.api.soup.presentation.dto.BrewSoupRequest
import com.zerost.api.soup.presentation.dto.BrewSoupResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Soup", description = "스프 제작 API")
@RestController
@RequestMapping("/api/v1/soups")
class SoupController(
    private val soupBrewingService: SoupBrewingService,
) {

    @Operation(
        summary = "스프 제작",
        description = "선택한 재료 조합으로 스프를 제작합니다. 재료 순서와 슬롯 수가 레시피와 일치해야 하며, 제작 성공 시 보유 재료가 차감되고 제작 이력이 저장됩니다.",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "스프 제작 성공"),
            SwaggerApiResponse(responseCode = "400", description = "잘못된 요청 또는 제작 재료 수가 올바르지 않음"),
            SwaggerApiResponse(responseCode = "404", description = "등록된 유저 또는 일치하는 레시피를 찾을 수 없음"),
            SwaggerApiResponse(responseCode = "409", description = "보유 재료 수량이 부족함"),
        ],
    )
    @PostMapping("/brew")
    fun brew(
        @Valid @RequestBody request: BrewSoupRequest,
        httpServletRequest: HttpServletRequest,
    ): ApiResponse<BrewSoupResponse> {
        val deviceId = httpServletRequest.getAttribute(DeviceConstants.DEVICE_ID_ATTRIBUTE) as String
        val response = soupBrewingService.brew(deviceId, request.ingredientIds)
        return ApiResponse.success(response)
    }
}
