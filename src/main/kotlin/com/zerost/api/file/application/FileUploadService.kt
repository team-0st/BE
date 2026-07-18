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

    fun upload(file: MultipartFile, directory: String, deviceId: String, missionId: Long): FileUploadResponse {
        validate(file)

        val fileKey = buildFileKey(
            directory = directory,
            deviceId = deviceId,
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
        deviceId: String,
        missionId: Long,
        originalFilename: String?,
    ): String {
        val datePath = LocalDate.now().format(dateFormatter)
        val extension = extractExtension(originalFilename)
        val normalizedDeviceId = normalizePathSegment(deviceId)

        return "$directory/$normalizedDeviceId/$missionId/$datePath/${UUID.randomUUID()}$extension"
    }

    private fun extractExtension(originalFilename: String?): String {
        val extension = originalFilename
            ?.substringAfterLast('.', "")
            ?.lowercase()
            ?.takeIf { it.isNotBlank() }
            ?: return ""

        return ".$extension"
    }

    fun validateMissionImageKey(deviceId: String, missionId: Long, fileKey: String) {
        val expectedPrefix = "missions/${normalizePathSegment(deviceId)}/$missionId/"
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

    private fun normalizePathSegment(value: String): String {
        return value.replace(Regex("[^A-Za-z0-9._-]"), "_")
    }
}
