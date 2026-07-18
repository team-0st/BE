package com.zerost.api.file.presentation

import com.zerost.api.common.response.ApiResponse
import com.zerost.api.file.application.FileUploadService
import com.zerost.api.file.presentation.dto.FileUploadResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import com.zerost.api.common.device.DeviceConstants

@Tag(name = "File", description = "공통 파일 업로드 API")
@RestController
@RequestMapping("/api/v1/files")
class FileController(
    private val fileUploadService: FileUploadService,
) {

    @Operation(
        summary = "미션 인증 이미지 업로드",
        description = "미션 인증 제출에 사용할 이미지를 업로드합니다. JPG, PNG, WEBP 형식만 허용하며 응답으로 presigned 조회 URL과 파일 키를 반환합니다.",
        requestBody = RequestBody(
            required = true,
            content = [
                Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    schema = Schema(type = "object"),
                ),
            ],
        ),
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "업로드 성공"),
            SwaggerApiResponse(responseCode = "400", description = "빈 파일, 파일 크기 초과, 지원하지 않는 파일 형식"),
        ],
    )
    @PostMapping("/upload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadMissionImage(
        @RequestPart file: MultipartFile,
        @RequestParam missionId: Long,
        httpServletRequest: HttpServletRequest,
    ): ApiResponse<FileUploadResponse> {
        val deviceId = httpServletRequest.getAttribute(DeviceConstants.DEVICE_ID_ATTRIBUTE) as String
        val response = fileUploadService.upload(file, "missions", deviceId, missionId)
        return ApiResponse.success(response)
    }
}
