package com.zerost.api.user.presentation

import com.zerost.api.common.device.DeviceConstants
import com.zerost.api.common.response.ApiResponse
import com.zerost.api.user.application.CompleteOnboardingCommand
import com.zerost.api.user.application.OnboardingService
import com.zerost.api.user.presentation.dto.CompleteOnboardingRequest
import com.zerost.api.user.presentation.dto.CompleteOnboardingResponse
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

@Tag(name = "User", description = "유저 등록 및 온보딩 관련 API")
@RestController
@RequestMapping("/api/v1/onboarding")
class OnboardingController(
    private val onboardingService: OnboardingService,
) {

    @Operation(
        summary = "온보딩 완료",
        description = "닉네임, 전화번호, 샵 선택 정보를 저장하고 온보딩을 완료합니다.",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "온보딩 완료 성공"),
            SwaggerApiResponse(responseCode = "400", description = "잘못된 요청"),
            SwaggerApiResponse(responseCode = "404", description = "등록된 유저 또는 샵을 찾을 수 없음"),
        ],
    )
    @PostMapping("/complete")
    fun complete(
        @Valid @RequestBody request: CompleteOnboardingRequest,
        httpServletRequest: HttpServletRequest,
    ): ApiResponse<CompleteOnboardingResponse> {
        val deviceId = httpServletRequest.getAttribute(DeviceConstants.DEVICE_ID_ATTRIBUTE) as String

        val command = CompleteOnboardingCommand(
            deviceId = deviceId,
            nickname = request.nickname,
            phoneNumber = request.phoneNumber,
            shopId = requireNotNull(request.shopId),
        )

        val response = onboardingService.complete(command)
        return ApiResponse.success(response)
    }
}
