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
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@Tag(name = "File", description = "공통 파일 업로드 API")
@RestController
@RequestMapping("/api/v1/files")
class FileController(
    private val fileUploadService: FileUploadService,
) {

    @Operation(
        summary = "미션 인증 이미지 업로드",
        description = "미션 인증 제출에 사용할 이미지를 업로드합니다.",
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
            SwaggerApiResponse(responseCode = "400", description = "잘못된 요청"),
        ],
    )
    @PostMapping("/upload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadMissionImage(
        @RequestPart file: MultipartFile,
    ): ApiResponse<FileUploadResponse> {
        val response = fileUploadService.upload(file, "missions")
        return ApiResponse.success(response)
    }
}
