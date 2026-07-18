package com.zerost.api.file.application

import com.zerost.api.common.config.S3Properties
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

    fun upload(file: MultipartFile, directory: String): FileUploadResponse {
        val today = LocalDate.now()
        val fileKey = buildFileKey(directory, today)

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

    private fun buildFileKey(directory: String, today: LocalDate): String {
        return "$directory/${today.year}/${today.monthValue}/${today.dayOfMonth}/${UUID.randomUUID()}"
    }
}
