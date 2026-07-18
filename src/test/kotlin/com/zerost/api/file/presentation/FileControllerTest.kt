package com.zerost.api.file.presentation

import com.zerost.api.common.device.DeviceIdInterceptor
import com.zerost.api.common.exception.GlobalExceptionHandler
import com.zerost.api.file.application.FileUploadService
import com.zerost.api.file.presentation.dto.FileUploadResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class FileControllerTest {

    private val fileUploadService = mock(FileUploadService::class.java)
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
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
        `when`(fileUploadService.upload(anyFile(), anyDirectory())).thenReturn(
            FileUploadResponse(
                fileUrl = "https://test-bucket.s3.ap-northeast-2.amazonaws.com/missions/2026/7/18/file.jpg",
                fileKey = "missions/2026/7/18/file.jpg",
            ),
        )

        mockMvc.perform(
            multipart("/api/v1/files/upload")
                .file(file)
                .header("X-Device-Id", "device-1"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.fileKey").value("missions/2026/7/18/file.jpg"))

        verify(fileUploadService).upload(anyFile(), anyDirectory())
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
                .file(file),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("DEVICE_ID_HEADER_MISSING"))
    }

    @Suppress("UNCHECKED_CAST")
    private fun anyFile(): org.springframework.web.multipart.MultipartFile {
        ArgumentMatchers.any(org.springframework.web.multipart.MultipartFile::class.java)
        return uninitialized()
    }

    @Suppress("UNCHECKED_CAST")
    private fun anyDirectory(): String {
        ArgumentMatchers.anyString()
        return uninitialized()
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> uninitialized(): T = null as T
}
