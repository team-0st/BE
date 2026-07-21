package com.zerost.api.auth.presentation

import com.zerost.api.auth.application.AuthLoginService
import com.zerost.api.auth.presentation.dto.LoginRequest
import com.zerost.api.auth.presentation.dto.LoginResponse
import com.zerost.api.common.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Auth", description = "로그인 API")
@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authLoginService: AuthLoginService,
) {

    @Operation(
        summary = "휴대전화 번호 로그인",
        description = "휴대전화 번호와 비밀번호를 검증해 로그인합니다. 현재 서비스는 이후 요청 식별에 X-Device-Id를 사용하므로 로그인 성공 시 등록된 deviceId를 함께 반환합니다.",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "로그인 성공"),
            SwaggerApiResponse(responseCode = "400", description = "잘못된 요청"),
            SwaggerApiResponse(responseCode = "401", description = "휴대전화 번호 또는 비밀번호가 올바르지 않음"),
        ],
    )
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest,
    ): ApiResponse<LoginResponse> {
        val response = authLoginService.login(request.phoneNumber, request.password)
        return ApiResponse.success(response)
    }
}
