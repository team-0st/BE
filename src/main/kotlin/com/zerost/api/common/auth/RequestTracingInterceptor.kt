package com.zerost.api.common.auth

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import java.util.UUID

@Component
class RequestTracingInterceptor : HandlerInterceptor {

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        val traceId = request.getHeader(REQUEST_ID_HEADER)
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: UUID.randomUUID().toString()

        request.setAttribute(AuthRequestConstants.TRACE_ID_ATTRIBUTE, traceId)
        response.setHeader(REQUEST_ID_HEADER, traceId)
        MDC.put(MDC_TRACE_ID_KEY, traceId)
        return true
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?,
    ) {
        MDC.remove(MDC_TRACE_ID_KEY)
    }

    companion object {
        private const val REQUEST_ID_HEADER = "X-Request-Id"
        private const val MDC_TRACE_ID_KEY = "traceId"
    }
}
