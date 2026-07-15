package com.zerost.api.common.exception

import com.zerost.api.common.response.ApiErrorResponse
import com.zerost.api.common.response.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(ex: BusinessException): ResponseEntity<ApiResponse<Nothing>> {
        val errorCode = ex.errorCode
        return ResponseEntity
            .status(errorCode.httpStatus)
            .body(
                ApiResponse.failure(
                    ApiErrorResponse(
                        code = errorCode.code,
                        message = errorCode.message
                    )
                )
            )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Nothing>> {
        val errorCode = ErrorCode.INVALID_INPUT_VALUE
        val message = ex.bindingResult.fieldErrors.firstOrNull()?.defaultMessage ?: errorCode.message

        return ResponseEntity
            .status(errorCode.httpStatus)
            .body(
                ApiResponse.failure(
                    ApiErrorResponse(
                        code = errorCode.code,
                        message = message
                    )
                )
            )
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotAllowed(ex: HttpRequestMethodNotSupportedException): ResponseEntity<ApiResponse<Nothing>> {
        val errorCode = ErrorCode.METHOD_NOT_ALLOWED
        return ResponseEntity
            .status(errorCode.httpStatus)
            .body(
                ApiResponse.failure(
                    ApiErrorResponse(
                        code = errorCode.code,
                        message = errorCode.message
                    )
                )
            )
    }

    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<ApiResponse<Nothing>> {
        val errorCode = ErrorCode.INTERNAL_SERVER_ERROR
        return ResponseEntity
            .status(errorCode.httpStatus)
            .body(
                ApiResponse.failure(
                    ApiErrorResponse(
                        code = errorCode.code,
                        message = errorCode.message
                    )
                )
            )
    }
}
