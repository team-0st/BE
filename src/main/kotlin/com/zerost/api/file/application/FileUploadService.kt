package com.zerost.api.file.application

import com.zerost.api.common.config.S3Properties
import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.file.presentation.dto.FileUploadResponse
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.time.LocalDate
import java.util.UUID

@Service
class FileUploadService(
    private val s3Client: S3Client,
    private val s3Properties: S3Properties,
) {
    private val allowedContentTypes = setOf(
        "image/jpeg",
        "image/png",
        "image/webp",
    )

    fun upload(file: MultipartFile, directory: String): FileUploadResponse {
        validate(file)

        val fileKey = buildFileKey(
            directory = directory,
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

        val fileUrl = "https://${s3Properties.bucket}.s3.${s3Properties.region}.amazonaws.com/$fileKey"

        return FileUploadResponse(
            fileUrl = fileUrl,
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
        originalFilename: String?,
    ): String {
        val today = LocalDate.now()
        val extension = extractExtension(originalFilename)

        return "$directory/${today.year}/${today.monthValue}/${today.dayOfMonth}/${UUID.randomUUID()}$extension"
    }

    private fun extractExtension(originalFilename: String?): String {
        val extension = originalFilename
            ?.substringAfterLast('.', "")
            ?.lowercase()
            ?.takeIf { it.isNotBlank() }
            ?: return ""

        return ".$extension"
    }
}
