package com.zerost.api.file.application

import com.zerost.api.common.config.S3Properties
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.mock.web.MockMultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import kotlin.test.assertTrue

class FileUploadServiceTest {

    private val s3Client = mock(S3Client::class.java)
    private val s3Properties = S3Properties(
        bucket = "test-bucket",
        region = "ap-northeast-2",
    )
    private val fileUploadService = FileUploadService(
        s3Client = s3Client,
        s3Properties = s3Properties,
    )

    @Test
    fun `미션 디렉터리로 파일을 업로드할 수 있다`() {
        val file = MockMultipartFile(
            "file",
            "mission.jpg",
            "image/jpeg",
            "image-content".toByteArray(),
        )
        `when`(s3Client.putObject(any(PutObjectRequest::class.java), any(RequestBody::class.java))).thenReturn(null)

        val response = fileUploadService.upload(file, "missions")

        assertTrue(response.fileKey.startsWith("missions/"))
        assertTrue(response.fileUrl.startsWith("https://test-bucket.s3.ap-northeast-2.amazonaws.com/missions/"))
        verify(s3Client).putObject(any(PutObjectRequest::class.java), any(RequestBody::class.java))
    }
}
