package com.zerost.api.file.presentation

import com.zerost.api.common.config.S3Properties
import com.zerost.api.common.device.DeviceIdInterceptor
import com.zerost.api.common.exception.GlobalExceptionHandler
import com.zerost.api.file.application.FileUploadService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest

class FileControllerTest {

    private val s3Client = mock(S3Client::class.java)
    private val s3Presigner = mock(S3Presigner::class.java)
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        val s3Properties = S3Properties(
            bucket = "test-bucket",
            region = "ap-northeast-2",
            maxFileSize = 5 * 1024 * 1024,
            presignedUrlDurationSeconds = 600,
        )
        val fileUploadService = FileUploadService(
            s3Client = s3Client,
            s3Properties = s3Properties,
            s3Presigner = s3Presigner,
        )

        mockMvc = MockMvcBuilders.standaloneSetup(FileController(fileUploadService))
            .setControllerAdvice(GlobalExceptionHandler())
            .addInterceptors(DeviceIdInterceptor())
            .build()
    }

    @Test
    fun `디바이스 아이디가 있으면 미션 인증 이미지를 업로드할 수 있다`() {
        val file = MockMultipartFile(
            "file",
            "mission.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "image-content".toByteArray(),
        )
        val presignedRequest = mock(PresignedGetObjectRequest::class.java)
        `when`(s3Client.putObject(any(PutObjectRequest::class.java), any(RequestBody::class.java))).thenReturn(null)
        `when`(s3Presigner.presignGetObject(any(GetObjectPresignRequest::class.java))).thenReturn(presignedRequest)
        `when`(presignedRequest.url()).thenReturn(java.net.URI.create("https://signed.example.com/mission.jpg").toURL())

        mockMvc.perform(
            multipart("/api/v1/files/upload")
                .file(file)
                .param("missionId", "1")
                .header("X-Device-Id", "device-1"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.fileUrl").value("https://signed.example.com/mission.jpg"))
            .andExpect(jsonPath("$.data.fileKey").value(org.hamcrest.Matchers.matchesPattern("missions/device-1/1/\\d{4}/\\d{2}/\\d{2}/.+\\.jpg")))
    }

    @Test
    fun `디바이스 아이디가 없으면 파일 업로드에 실패한다`() {
        val file = MockMultipartFile(
            "file",
            "mission.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "image-content".toByteArray(),
        )

        mockMvc.perform(
            multipart("/api/v1/files/upload")
                .file(file)
                .param("missionId", "1"),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("DEVICE_ID_HEADER_MISSING"))
    }
}
