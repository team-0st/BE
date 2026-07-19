package com.zerost.api.gacha.presentation

import com.zerost.api.common.device.DeviceConstants
import com.zerost.api.common.response.ApiResponse
import com.zerost.api.gacha.application.GachaExecutionService
import com.zerost.api.gacha.presentation.dto.ExecuteGachaResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Gacha", description = "가챠 실행 API")
@RestController
@RequestMapping("/api/v1/gachas")
class GachaController(
    private val gachaExecutionService: GachaExecutionService,
) {

    @Operation(
        summary = "가챠 1회 실행",
        description = "에코잼 100개를 사용해 가챠를 1회 실행합니다. 실행 시 보유 에코잼이 차감되고, 활성화된 보상 정책 중 하나가 확률에 따라 선택됩니다.",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "가챠 실행 성공"),
            SwaggerApiResponse(responseCode = "404", description = "등록된 유저 또는 활성화된 가챠 보상 정책을 찾을 수 없음"),
            SwaggerApiResponse(responseCode = "409", description = "보유 에코잼이 부족함"),
        ],
    )
    @PostMapping("/draw")
    fun execute(
        httpServletRequest: HttpServletRequest,
    ): ApiResponse<ExecuteGachaResponse> {
        val deviceId = httpServletRequest.getAttribute(DeviceConstants.DEVICE_ID_ATTRIBUTE) as String
        val response = gachaExecutionService.execute(deviceId)
        return ApiResponse.success(response)
    }
}
