package com.zerost.api.checkin.presentation

import com.zerost.api.checkin.application.CheckInService
import com.zerost.api.checkin.presentation.dto.CheckInResponse
import com.zerost.api.common.device.DeviceConstants
import com.zerost.api.common.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "CheckIn", description = "출석 관련 API")
@RestController
@RequestMapping("/api/v1/check-in")
class CheckInController(
    private val checkInService: CheckInService,
) {

    @Operation(
        summary = "오늘 출석",
        description = "오늘 출석을 처리하고 보상 재료를 지급합니다.",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "출석 성공"),
            SwaggerApiResponse(responseCode = "409", description = "오늘 이미 출석함"),
            SwaggerApiResponse(responseCode = "404", description = "등록된 유저 또는 재료를 찾을 수 없음"),
        ],
    )
    @PostMapping
    fun checkIn(
        httpServletRequest: HttpServletRequest,
    ): ApiResponse<CheckInResponse> {
        val deviceId = httpServletRequest.getAttribute(DeviceConstants.DEVICE_ID_ATTRIBUTE) as String
        val response = checkInService.checkIn(deviceId)
        return ApiResponse.success(response)
    }
}
