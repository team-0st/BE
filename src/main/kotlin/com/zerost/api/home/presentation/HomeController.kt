package com.zerost.api.home.presentation

import com.zerost.api.common.auth.AuthRequestConstants
import com.zerost.api.common.response.ApiResponse
import com.zerost.api.home.application.HomeQueryService
import com.zerost.api.home.presentation.dto.HomeResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Home", description = "홈 화면 통합 조회 API")
@RestController
@RequestMapping("/api/v1/home")
class HomeController(
    private val homeQueryService: HomeQueryService,
) {

    @Operation(
        summary = "홈 화면 통합 조회",
        description = "현재 유저의 자산, 오늘 출석 여부, 오늘 미션 진행 현황을 한 번에 조회합니다.",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "홈 화면 조회 성공"),
            SwaggerApiResponse(responseCode = "404", description = "등록된 유저를 찾을 수 없음"),
        ],
    )
    @GetMapping
    fun getHome(
        httpServletRequest: HttpServletRequest,
    ): ApiResponse<HomeResponse> {
        val userId = httpServletRequest.getAttribute(AuthRequestConstants.USER_ID_ATTRIBUTE) as Long
        val response = homeQueryService.getHome(userId)
        return ApiResponse.success(response)
    }
}
