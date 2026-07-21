package com.zerost.api.user.presentation

import com.zerost.api.common.auth.AuthRequestConstants
import com.zerost.api.common.response.ApiResponse
import com.zerost.api.user.application.NicknameCommandService
import com.zerost.api.user.application.UserRegistrationService
import com.zerost.api.user.presentation.dto.UpdateNicknameRequest
import com.zerost.api.user.presentation.dto.UpdateNicknameResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "User", description = "유저 등록 및 온보딩 관련 API")
@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userRegistrationService: UserRegistrationService,
    private val nicknameCommandService: NicknameCommandService,
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

    @Operation(
        summary = "닉네임 변경",
        description = "현재 로그인한 유저의 닉네임을 변경합니다."
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "닉네임 변경 성공"),
            SwaggerApiResponse(responseCode = "400", description = "잘못된 요청"),
            SwaggerApiResponse(responseCode = "404", description = "등록된 유저를 찾을 수 없음"),
            SwaggerApiResponse(responseCode = "409", description = "이미 사용 중인 닉네임"),
        ]
    )
    @PatchMapping("/me/nickname")
    fun updateNickname(
        @Valid @RequestBody request: UpdateNicknameRequest,
        httpServletRequest: HttpServletRequest,
    ): ApiResponse<UpdateNicknameResponse> {
        val userId = httpServletRequest.getAttribute(AuthRequestConstants.USER_ID_ATTRIBUTE) as Long
        return ApiResponse.success(
            nicknameCommandService.updateNickname(userId, request.nickname),
        )
    }
}
