package com.zerost.api.soup.presentation

import com.zerost.api.common.auth.AuthRequestConstants
import com.zerost.api.common.response.ApiResponse
import com.zerost.api.soup.application.SoupBrewingService
import com.zerost.api.soup.application.SoupRerollService
import com.zerost.api.soup.presentation.dto.BrewSoupRequest
import com.zerost.api.soup.presentation.dto.BrewSoupResponse
import com.zerost.api.soup.presentation.dto.RerollSoupResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Soup", description = "스프 제작 API")
@RestController
@RequestMapping("/api/v1/soups")
class SoupController(
    private val soupBrewingService: SoupBrewingService,
    private val soupRerollService: SoupRerollService,
) {

    @Operation(
        summary = "스프 제작",
        description = "선택한 재료 조합으로 스프를 제작합니다. 재료 순서와 슬롯 수가 레시피와 일치해야 하며, 2슬롯 입문 스프를 포함해 제작 성공 시 보유 재료가 차감되고 제작 이력이 저장됩니다.",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "스프 제작 성공"),
            SwaggerApiResponse(responseCode = "400", description = "잘못된 요청 또는 제작 재료 수가 올바르지 않음"),
            SwaggerApiResponse(responseCode = "404", description = "등록된 유저 또는 일치하는 레시피를 찾을 수 없음"),
            SwaggerApiResponse(responseCode = "409", description = "보유 재료 수량이 부족함"),
        ],
    )
    @PostMapping("/brew")
    fun brew(
        @Valid @RequestBody request: BrewSoupRequest,
        httpServletRequest: HttpServletRequest,
    ): ApiResponse<BrewSoupResponse> {
        val userId = httpServletRequest.getAttribute(AuthRequestConstants.USER_ID_ATTRIBUTE) as Long
        val response = soupBrewingService.brew(userId, request.ingredientIds)
        return ApiResponse.success(response)
    }

    @Operation(
        summary = "스프 보상 리롤",
        description = "이미 제작한 스프의 보상을 에코잼을 사용해 1회에 한해 다시 추첨합니다. 기존 보상은 회수되고 새 보상이 최종 결과로 적용됩니다.",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "스프 리롤 성공"),
            SwaggerApiResponse(responseCode = "404", description = "등록된 유저 또는 스프 제작 정보를 찾을 수 없음"),
            SwaggerApiResponse(responseCode = "409", description = "이미 리롤을 완료했거나 현재 보상 등급에서 리롤할 수 없거나 보유 에코잼이 부족함"),
        ],
    )
    @PostMapping("/{soupId}/reroll")
    fun reroll(
        @PathVariable soupId: Long,
        httpServletRequest: HttpServletRequest,
    ): ApiResponse<RerollSoupResponse> {
        val userId = httpServletRequest.getAttribute(AuthRequestConstants.USER_ID_ATTRIBUTE) as Long
        val response = soupRerollService.reroll(userId, soupId)
        return ApiResponse.success(response)
    }
}
