package com.zerost.api.profile.presentation

import com.zerost.api.common.auth.AuthRequestConstants
import com.zerost.api.common.response.ApiResponse
import com.zerost.api.profile.application.ProfileCharacterCommandService
import com.zerost.api.profile.application.ProfileCharacterQueryService
import com.zerost.api.profile.presentation.dto.ProfileCharacterResponse
import com.zerost.api.profile.presentation.dto.UpdateProfileCharacterRequest
import com.zerost.api.profile.presentation.dto.UpdateProfileCharacterResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "ProfileCharacter", description = "선택형 프로필 캐릭터 API")
@RestController
@RequestMapping("/api/v1/profile-characters")
class ProfileCharacterController(
    private val profileCharacterQueryService: ProfileCharacterQueryService,
    private val profileCharacterCommandService: ProfileCharacterCommandService,
) {

    @Operation(
        summary = "선택 가능한 프로필 캐릭터 목록 조회",
        description = "유저가 선택할 수 있는 프로필 캐릭터 목록을 조회합니다.",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "조회 성공"),
        ],
    )
    @GetMapping
    fun getProfileCharacters(): ApiResponse<List<ProfileCharacterResponse>> {
        return ApiResponse.success(profileCharacterQueryService.getProfileCharacters())
    }

    @Operation(
        summary = "프로필 캐릭터 선택",
        description = "현재 로그인한 유저의 프로필 캐릭터를 선택하거나 변경합니다.",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "선택 성공"),
            SwaggerApiResponse(responseCode = "400", description = "잘못된 요청 또는 유효하지 않은 프로필 캐릭터 코드"),
            SwaggerApiResponse(responseCode = "404", description = "등록된 유저를 찾을 수 없음"),
        ],
    )
    @PatchMapping("/me")
    fun updateProfileCharacter(
        @Valid @RequestBody request: UpdateProfileCharacterRequest,
        httpServletRequest: HttpServletRequest,
    ): ApiResponse<UpdateProfileCharacterResponse> {
        val userId = httpServletRequest.getAttribute(AuthRequestConstants.USER_ID_ATTRIBUTE) as Long
        return ApiResponse.success(
            profileCharacterCommandService.updateProfileCharacter(userId, request.profileCharacterCode),
        )
    }
}
