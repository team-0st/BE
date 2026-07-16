package com.zerost.api.user.presentation

import com.zerost.api.common.response.ApiResponse
import com.zerost.api.user.application.UserRegistrationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "User", description = "유저 등록 및 온보딩 관련 API")
@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userRegistrationService: UserRegistrationService,
) {

    @Operation(
        summary = "디바이스 등록",
        description = "앱 첫 실행 시 디바이스 식별자로 유저를 등록합니다. 이미 등록된 디바이스라면 기존 유저 정보를 반환합니다."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "디바이스 등록 성공"),
            SwaggerApiResponse(responseCode = "400", description = "잘못된 요청")
        ]
    )
    @PostMapping("/register")
    fun register(
        @Valid @RequestBody request: RegisterUserRequest
    ): ApiResponse<RegisterUserResponse> {
        val response = userRegistrationService.register(request.deviceId)
        return ApiResponse.success(response)
    }
}
