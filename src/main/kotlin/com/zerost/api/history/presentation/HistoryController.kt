package com.zerost.api.history.presentation

import com.zerost.api.common.auth.AuthRequestConstants
import com.zerost.api.common.response.ApiResponse
import com.zerost.api.history.application.HistoryQueryService
import com.zerost.api.history.presentation.dto.AssetHistoryResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "History", description = "에코잼 및 포인트 적립 내역 조회 API")
@RestController
@RequestMapping("/api/v1/histories")
class HistoryController(
    private val historyQueryService: HistoryQueryService,
) {

    @Operation(
        summary = "에코잼 적립 내역 조회",
        description = "현재 유저의 에코잼 적립 내역을 최신순으로 조회합니다.",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "조회 성공"),
            SwaggerApiResponse(responseCode = "404", description = "등록된 유저를 찾을 수 없음"),
        ],
    )
    @GetMapping("/eco-jams")
    fun getEcoJamHistories(
        httpServletRequest: HttpServletRequest,
    ): ApiResponse<List<AssetHistoryResponse>> {
        val userId = httpServletRequest.getAttribute(AuthRequestConstants.USER_ID_ATTRIBUTE) as Long
        val response = historyQueryService.getEcoJamHistories(userId)
        return ApiResponse.success(response)
    }

    @Operation(
        summary = "포인트 적립 내역 조회",
        description = "현재 유저의 포인트 적립 내역을 최신순으로 조회합니다.",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "조회 성공"),
            SwaggerApiResponse(responseCode = "404", description = "등록된 유저를 찾을 수 없음"),
        ],
    )
    @GetMapping("/points")
    fun getPointHistories(
        httpServletRequest: HttpServletRequest,
    ): ApiResponse<List<AssetHistoryResponse>> {
        val userId = httpServletRequest.getAttribute(AuthRequestConstants.USER_ID_ATTRIBUTE) as Long
        val response = historyQueryService.getPointHistories(userId)
        return ApiResponse.success(response)
    }
}
