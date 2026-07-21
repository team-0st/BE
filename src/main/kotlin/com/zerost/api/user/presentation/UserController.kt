package com.zerost.api.user.presentation

import com.zerost.api.common.response.ApiResponse
import com.zerost.api.user.application.UserRegistrationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "User", description = "유저 등록 및 온보딩 관련 API")
@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userRegistrationService: UserRegistrationService,
) {

    @Operation(
        summary = "임시 유저 등록",
        description = "앱 첫 실행 시 임시 유저를 생성하고 온보딩 전용 access token과 refresh token을 발급합니다."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "임시 유저 등록 성공"),
        ]
    )
    @PostMapping("/register")
    fun register(): ApiResponse<RegisterUserResponse> {
        val response = userRegistrationService.register()
        return ApiResponse.success(response)
    }
}
