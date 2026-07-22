package com.zerost.api.admin.asset.presentation

import com.zerost.api.admin.asset.application.AdminAssetGrantService
import com.zerost.api.admin.asset.presentation.dto.AdminAssetGrantRequest
import com.zerost.api.admin.asset.presentation.dto.AdminAssetGrantResponse
import com.zerost.api.admin.asset.presentation.dto.AdminUserSummaryResponse
import com.zerost.api.common.auth.AuthRequestConstants
import com.zerost.api.common.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Admin Assets", description = "관리자 에코잼·알맹 포인트 지급 API")
@RestController
@RequestMapping("/api/v1/admin")
class AdminAssetGrantController(
    private val adminAssetGrantService: AdminAssetGrantService,
) {

    @Operation(summary = "유저 목록 조회 (ADMIN) — 지급 대상 선택용")
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "조회 성공"),
            SwaggerApiResponse(responseCode = "403", description = "관리자 권한이 없는 요청"),
        ],
    )
    @GetMapping("/users")
    fun listUsers(request: HttpServletRequest): ApiResponse<List<AdminUserSummaryResponse>> {
        log.info(
            "admin_users_list_requested traceId={} adminUserId={}",
            request.getAttribute(AuthRequestConstants.TRACE_ID_ATTRIBUTE),
            request.getAttribute(AuthRequestConstants.USER_ID_ATTRIBUTE),
        )
        return ApiResponse.success(adminAssetGrantService.listUsers())
    }

    @Operation(summary = "에코잼 또는 알맹 포인트 지급 (ADMIN)")
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "지급 성공"),
            SwaggerApiResponse(responseCode = "400", description = "잘못된 요청"),
            SwaggerApiResponse(responseCode = "403", description = "관리자 권한이 없는 요청"),
            SwaggerApiResponse(responseCode = "404", description = "유저를 찾을 수 없음"),
        ],
    )
    @PostMapping("/assets/grant")
    fun grantAssets(
        @Valid @RequestBody body: AdminAssetGrantRequest,
        request: HttpServletRequest,
    ): ApiResponse<AdminAssetGrantResponse> {
        val adminUserId = request.getAttribute(AuthRequestConstants.USER_ID_ATTRIBUTE) as Long
        log.info(
            "admin_asset_grant_requested traceId={} adminUserId={} assetType={} userIds={} amount={}",
            request.getAttribute(AuthRequestConstants.TRACE_ID_ATTRIBUTE),
            adminUserId,
            body.assetType,
            body.userIds,
            body.amount,
        )
        return ApiResponse.success(adminAssetGrantService.grant(adminUserId, body))
    }

    companion object {
        private val log = LoggerFactory.getLogger(AdminAssetGrantController::class.java)
    }
}
