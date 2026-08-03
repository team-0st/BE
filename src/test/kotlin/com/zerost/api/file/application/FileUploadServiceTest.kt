package com.zerost.api.file.application

import com.zerost.api.common.config.S3Properties
import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.mock.web.MockMultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest
import kotlin.test.assertTrue

class FileUploadServiceTest {

    private val s3Client = mock(S3Client::class.java)
    private val s3Presigner = mock(S3Presigner::class.java)
    private val s3Properties = S3Properties(
        bucket = "test-bucket",
        region = "ap-northeast-2",
        maxFileSize = 5 * 1024 * 1024,
        presignedUrlDurationSeconds = 600,
    )
    private val fileUploadService = FileUploadService(
        s3Client = s3Client,
        s3Properties = s3Properties,
        s3Presigner = s3Presigner,
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
        val presignedRequest = mock(PresignedGetObjectRequest::class.java)
        `when`(s3Presigner.presignGetObject(any(GetObjectPresignRequest::class.java))).thenReturn(presignedRequest)
        `when`(presignedRequest.url()).thenReturn(java.net.URI.create("https://signed.example.com/mission.jpg").toURL())

        val response = fileUploadService.upload(file, "missions", 1L, 1L)

        assertTrue(response.fileKey.startsWith("missions/1/1/"))
        assertTrue(response.fileKey.endsWith(".jpg"))
        assertTrue(response.fileUrl.startsWith("https://signed.example.com/"))
        verify(s3Client).putObject(any(PutObjectRequest::class.java), any(RequestBody::class.java))
    }

    @Test
    fun `빈 파일이면 업로드할 수 없다`() {
        val file = MockMultipartFile(
            "file",
            "empty.jpg",
            "image/jpeg",
            ByteArray(0),
        )

        val exception = assertThrows<BusinessException> {
            fileUploadService.upload(file, "missions", 1L, 1L)
        }

        kotlin.test.assertEquals(ErrorCode.EMPTY_FILE, exception.errorCode)
    }

    @Test
    fun `허용하지 않는 형식이면 업로드할 수 없다`() {
        val file = MockMultipartFile(
            "file",
            "mission.gif",
            "image/gif",
            "image-content".toByteArray(),
        )

        val exception = assertThrows<BusinessException> {
            fileUploadService.upload(file, "missions", 1L, 1L)
        }

        kotlin.test.assertEquals(ErrorCode.INVALID_FILE_TYPE, exception.errorCode)
    }

    @Test
    fun `최대 크기를 초과하면 업로드할 수 없다`() {
        val file = MockMultipartFile(
            "file",
            "large.jpg",
            "image/jpeg",
            ByteArray((5 * 1024 * 1024) + 1),
        )

        val exception = assertThrows<BusinessException> {
            fileUploadService.upload(file, "missions", 1L, 1L)
        }

        kotlin.test.assertEquals(ErrorCode.FILE_SIZE_EXCEEDED, exception.errorCode)
    }
}
