package com.zerost.api.testerlink.presentation

import com.zerost.api.common.response.ApiResponse
import com.zerost.api.testerlink.application.TesterLinkService
import com.zerost.api.testerlink.presentation.dto.CurrentTesterLinkResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Tester Link", description = "공개 테스트 공유 링크 API")
@RestController
@RequestMapping("/api/v1/tester-link")
class TesterLinkController(
    private val testerLinkService: TesterLinkService,
) {

    @Operation(summary = "최신 deep link 조회 (인증 없음)")
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "조회 성공"),
        ],
    )
    @GetMapping("/current")
    fun getCurrentTesterLink(): ApiResponse<CurrentTesterLinkResponse> {
        return ApiResponse.success(testerLinkService.getCurrentTesterLink())
    }
}
