package com.zerost.api.auth.presentation

import com.zerost.api.auth.application.AuthLoginService
import com.zerost.api.auth.application.AuthTokenService
import com.zerost.api.auth.presentation.dto.LoginRequest
import com.zerost.api.auth.presentation.dto.LoginResponse
import com.zerost.api.auth.presentation.dto.LogoutRequest
import com.zerost.api.auth.presentation.dto.RefreshTokenRequest
import com.zerost.api.auth.presentation.dto.RefreshTokenResponse
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
    private val authTokenService: AuthTokenService,
) {

    @Operation(
        summary = "휴대전화 번호 로그인",
        description = "휴대전화 번호와 비밀번호를 검증해 access token과 refresh token을 발급합니다.",
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

    @Operation(
        summary = "토큰 재발급",
        description = "유효한 refresh token으로 access token과 refresh token을 재발급합니다.",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
            SwaggerApiResponse(responseCode = "400", description = "잘못된 요청"),
            SwaggerApiResponse(responseCode = "401", description = "유효하지 않은 refresh token"),
        ],
    )
    @PostMapping("/refresh")
    fun refresh(
        @Valid @RequestBody request: RefreshTokenRequest,
    ): ApiResponse<RefreshTokenResponse> {
        val response = authTokenService.refresh(request.refreshToken)
        return ApiResponse.success(response)
    }

    @Operation(
        summary = "로그아웃",
        description = "refresh token을 폐기해 로그아웃합니다.",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "로그아웃 성공"),
            SwaggerApiResponse(responseCode = "400", description = "잘못된 요청"),
            SwaggerApiResponse(responseCode = "401", description = "유효하지 않은 refresh token"),
        ],
    )
    @PostMapping("/logout")
    fun logout(
        @Valid @RequestBody request: LogoutRequest,
    ): ApiResponse<Unit> {
        authTokenService.logout(request.refreshToken)
        return ApiResponse.success(Unit)
    }
}
