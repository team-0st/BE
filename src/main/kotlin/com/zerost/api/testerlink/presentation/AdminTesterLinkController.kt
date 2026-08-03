package com.zerost.api.testerlink.presentation

import com.zerost.api.common.auth.AuthRequestConstants
import com.zerost.api.common.response.ApiResponse
import com.zerost.api.testerlink.application.TesterLinkService
import com.zerost.api.testerlink.presentation.dto.AdminTesterLinkResponse
import com.zerost.api.testerlink.presentation.dto.UpdateTesterLinkRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Admin Tester Link", description = "관리자 테스트 공유 링크 API")
@RestController
@RequestMapping("/api/v1/admin/tester-link")
class AdminTesterLinkController(
    private val testerLinkService: TesterLinkService,
) {

    @Operation(summary = "현재 테스트 링크 조회 (ADMIN)")
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "조회 성공"),
            SwaggerApiResponse(responseCode = "403", description = "관리자 권한이 없는 요청"),
        ],
    )
    @GetMapping
    fun getTesterLink(request: HttpServletRequest): ApiResponse<AdminTesterLinkResponse> {
        log.info(
            "admin_tester_link_get_requested traceId={} adminUserId={}",
            request.getAttribute(AuthRequestConstants.TRACE_ID_ATTRIBUTE),
            request.getAttribute(AuthRequestConstants.USER_ID_ATTRIBUTE),
        )
        return ApiResponse.success(testerLinkService.getAdminTesterLink())
    }

    @Operation(summary = "테스트 링크 갱신 (ADMIN)")
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "갱신 성공"),
            SwaggerApiResponse(responseCode = "400", description = "deepLink 또는 tossShareUrl 형식 오류"),
            SwaggerApiResponse(responseCode = "403", description = "관리자 권한이 없는 요청"),
        ],
    )
    @PutMapping
    fun updateTesterLink(
        @Valid @RequestBody body: UpdateTesterLinkRequest,
        request: HttpServletRequest,
    ): ApiResponse<AdminTesterLinkResponse> {
        val adminUserId = request.getAttribute(AuthRequestConstants.USER_ID_ATTRIBUTE) as Long
        log.info(
            "admin_tester_link_update_requested traceId={} adminUserId={}",
            request.getAttribute(AuthRequestConstants.TRACE_ID_ATTRIBUTE),
            adminUserId,
        )
        val response = testerLinkService.updateTesterLink(
            body.deepLink,
            body.tossShareUrl,
            adminUserId,
        )
        return ApiResponse.success(response)
    }

    companion object {
        private val log = LoggerFactory.getLogger(AdminTesterLinkController::class.java)
    }
}
