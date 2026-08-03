package com.zerost.api.file.application

import com.zerost.api.common.config.S3Properties
import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.file.presentation.dto.FileUploadResponse
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.HeadObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.S3Exception
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
class FileUploadService(
    private val s3Client: S3Client,
    private val s3Properties: S3Properties,
    private val s3Presigner: S3Presigner,
) {
    private val allowedContentTypes = setOf(
        "image/jpeg",
        "image/png",
        "image/webp",
    )
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")

    fun upload(file: MultipartFile, directory: String, userId: Long, missionId: Long): FileUploadResponse {
        validate(file)

        val fileKey = buildFileKey(
            directory = directory,
            userId = userId,
            missionId = missionId,
            originalFilename = file.originalFilename,
        )

        val putObjectRequest = PutObjectRequest.builder()
            .bucket(s3Properties.bucket)
            .key(fileKey)
            .contentType(file.contentType)
            .build()

        s3Client.putObject(
            putObjectRequest,
            RequestBody.fromBytes(file.bytes),
        )

        return FileUploadResponse(
            fileUrl = createPresignedUrl(fileKey),
            fileKey = fileKey,
        )
    }

    private fun validate(file: MultipartFile) {
        if (file.isEmpty) {
            throw BusinessException(ErrorCode.EMPTY_FILE)
        }

        if (file.size > s3Properties.maxFileSize) {
            throw BusinessException(ErrorCode.FILE_SIZE_EXCEEDED)
        }

        if (file.contentType !in allowedContentTypes) {
            throw BusinessException(ErrorCode.INVALID_FILE_TYPE)
        }
    }

    private fun buildFileKey(
        directory: String,
        userId: Long,
        missionId: Long,
        originalFilename: String?,
    ): String {
        val datePath = LocalDate.now().format(dateFormatter)
        val extension = extractExtension(originalFilename)
        val normalizedUserId = userId.toString()

        return "$directory/$normalizedUserId/$missionId/$datePath/${UUID.randomUUID()}$extension"
    }

    private fun extractExtension(originalFilename: String?): String {
        val extension = originalFilename
            ?.substringAfterLast('.', "")
            ?.lowercase()
            ?.takeIf { it.isNotBlank() }
            ?: return ""

        return ".$extension"
    }

    fun validateMissionImageKey(userId: Long, missionId: Long, fileKey: String) {
        validateScopedImageKey("missions/$userId/$missionId/", fileKey)
    }

    fun validateCommunityMissionImageKey(userId: Long, communityMissionId: Long, fileKey: String) {
        validateScopedImageKey("community-missions/$userId/$communityMissionId/", fileKey)
    }

    private fun validateScopedImageKey(expectedPrefix: String, fileKey: String) {
        if (!fileKey.startsWith(expectedPrefix)) {
            throw BusinessException(ErrorCode.INVALID_FILE_KEY)
        }

        try {
            s3Client.headObject(
                HeadObjectRequest.builder()
                    .bucket(s3Properties.bucket)
                    .key(fileKey)
                    .build(),
            )
        } catch (ex: S3Exception) {
            if (ex.statusCode() == 404) {
                throw BusinessException(ErrorCode.UPLOADED_FILE_NOT_FOUND)
            }
            throw ex
        }
    }

    fun delete(fileKey: String) {
        try {
            s3Client.deleteObject(
                DeleteObjectRequest.builder()
                    .bucket(s3Properties.bucket)
                    .key(fileKey)
                    .build(),
            )
        } catch (ex: S3Exception) {
            if (ex.statusCode() == 404) {
                return
            }
            throw ex
        }
    }

    /** 검수 미리보기 등 — S3 객체에 대한 임시 GET URL */
    fun createPresignedGetUrl(fileKey: String): String = createPresignedUrl(fileKey)

    private fun createPresignedUrl(fileKey: String): String {
        val getObjectRequest = GetObjectRequest.builder()
            .bucket(s3Properties.bucket)
            .key(fileKey)
            .build()

        val presignRequest = GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofSeconds(s3Properties.presignedUrlDurationSeconds))
            .getObjectRequest(getObjectRequest)
            .build()

        return s3Presigner.presignGetObject(presignRequest).url().toString()
    }
}
