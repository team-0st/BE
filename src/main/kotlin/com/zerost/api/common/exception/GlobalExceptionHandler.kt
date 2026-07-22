package com.zerost.api.common.exception

import com.zerost.api.common.response.ApiErrorResponse
import com.zerost.api.common.response.ApiResponse
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(
        ex: BusinessException,
        request: jakarta.servlet.http.HttpServletRequest,
    ): ResponseEntity<ApiResponse<Nothing>> {
        val errorCode = ex.errorCode
        log.warn(
            "business_exception method={} path={} code={} message={}",
            request.method,
            request.requestURI,
            errorCode.code,
            errorCode.message,
        )
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
        val servletRequest = (request as? ServletWebRequest)?.request
        log.warn(
            "validation_exception method={} path={} code={} message={}",
            servletRequest?.method,
            servletRequest?.requestURI,
            errorCode.code,
            message,
        )

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
        val response = (request as? ServletWebRequest)?.response
        if (response?.isCommitted == true) {
            return ResponseEntity.status(statusCode).headers(headers).build()
        }

        val errorCode = when (statusCode.value()) {
            HttpServletResponse.SC_BAD_REQUEST -> ErrorCode.INVALID_INPUT_VALUE
            HttpServletResponse.SC_NOT_FOUND -> ErrorCode.RESOURCE_NOT_FOUND
            HttpServletResponse.SC_METHOD_NOT_ALLOWED -> ErrorCode.METHOD_NOT_ALLOWED
            HttpServletResponse.SC_NOT_ACCEPTABLE -> ErrorCode.NOT_ACCEPTABLE
            HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE -> ErrorCode.FILE_SIZE_EXCEEDED
            HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE -> ErrorCode.UNSUPPORTED_MEDIA_TYPE
            else -> ErrorCode.INTERNAL_SERVER_ERROR
        }
        val servletRequest = (request as? ServletWebRequest)?.request
        val logMessage = "internal_exception method={} path={} status={} code={}"
        if (statusCode.value() >= 500) {
            log.error(
                logMessage,
                servletRequest?.method,
                servletRequest?.requestURI,
                statusCode.value(),
                errorCode.code,
                ex,
            )
        } else {
            log.warn(
                logMessage,
                servletRequest?.method,
                servletRequest?.requestURI,
                statusCode.value(),
                errorCode.code,
            )
        }

        return ResponseEntity
            .status(statusCode)
            .headers(headers)
            .body(
                ApiResponse.failure(
                    ApiErrorResponse(
                        code = errorCode.code,
                        message = errorCode.message
                    )
                )
            )
    }

    companion object {
        private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    }
}
