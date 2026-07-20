package com.zerost.api.mypage.presentation

import com.zerost.api.common.device.DeviceConstants
import com.zerost.api.common.response.ApiResponse
import com.zerost.api.mypage.application.MyPageQueryService
import com.zerost.api.mypage.presentation.dto.MyPageResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "MyPage", description = "마이페이지 통합 조회 API")
@RestController
@RequestMapping("/api/v1/my-page")
class MyPageController(
    private val myPageQueryService: MyPageQueryService,
) {

    @Operation(
        summary = "마이페이지 통합 조회",
        description = "현재 유저의 프로필, 자산, 보유 재료, 제작 및 미션 통계를 한 번에 조회합니다.",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "마이페이지 조회 성공"),
            SwaggerApiResponse(responseCode = "404", description = "등록된 유저를 찾을 수 없음"),
        ],
    )
    @GetMapping
    fun getMyPage(
        httpServletRequest: HttpServletRequest,
    ): ApiResponse<MyPageResponse> {
        val deviceId = httpServletRequest.getAttribute(DeviceConstants.DEVICE_ID_ATTRIBUTE) as String
        val response = myPageQueryService.getMyPage(deviceId)
        return ApiResponse.success(response)
    }
}
