package com.zerost.api.common.device

import com.zerost.api.auth.application.AuthTokenProvider
import com.zerost.api.common.auth.AuthenticationInterceptor
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.servlet.HandlerInterceptor

class DeviceIdInterceptor(
    authTokenProvider: AuthTokenProvider,
) : HandlerInterceptor {
    private val delegate = AuthenticationInterceptor(authTokenProvider)

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean = delegate.preHandle(request, response, handler)
}
