package com.zerost.api.common.exception

import com.zerost.api.common.response.ApiErrorResponse
import com.zerost.api.common.response.ApiResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

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

    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any> {
        val errorCode = ErrorCode.INVALID_INPUT_VALUE
        val message = ex.bindingResult.fieldErrors.firstOrNull()?.defaultMessage ?: errorCode.message

        return ResponseEntity
            .status(errorCode.httpStatus)
            .headers(headers)
            .body(
                ApiResponse.failure(
                    ApiErrorResponse(
                        code = errorCode.code,
                        message = message
                    )
                )
            )
    }

    override fun handleExceptionInternal(
        ex: Exception,
        body: Any?,
        headers: HttpHeaders,
        statusCode: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any> {
        val response = when (statusCode.value()) {
            400 -> ApiResponse.failure(
                ApiErrorResponse(
                    code = ErrorCode.INVALID_INPUT_VALUE.code,
                    message = ErrorCode.INVALID_INPUT_VALUE.message
                )
            )
            405 -> ApiResponse.failure(
                ApiErrorResponse(
                    code = ErrorCode.METHOD_NOT_ALLOWED.code,
                    message = ErrorCode.METHOD_NOT_ALLOWED.message
                )
            )
            else -> ApiResponse.failure(
                ApiErrorResponse(
                    code = ErrorCode.INTERNAL_SERVER_ERROR.code,
                    message = ErrorCode.INTERNAL_SERVER_ERROR.message
                )
            )
        }

        return ResponseEntity
            .status(statusCode)
            .headers(headers)
            .body(response)
    }

    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(ex: RuntimeException): ResponseEntity<ApiResponse<Nothing>> {
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
