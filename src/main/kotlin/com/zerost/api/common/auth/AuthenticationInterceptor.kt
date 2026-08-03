package com.zerost.api.common.auth

import com.zerost.api.auth.application.AuthTokenProvider
import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class AuthenticationInterceptor(
    private val authTokenProvider: AuthTokenProvider,
) : HandlerInterceptor {

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        val authorization = request.getHeader("Authorization")?.trim()

        if (authorization.isNullOrEmpty()) {
            log.warn(
                "authentication_failed traceId={} method={} path={} reason=missing_authorization_header",
                request.getAttribute(AuthRequestConstants.TRACE_ID_ATTRIBUTE),
                request.method,
                request.requestURI,
            )
            throw BusinessException(ErrorCode.ACCESS_TOKEN_REQUIRED)
        }

        if (!authorization.startsWith("Bearer ")) {
            log.warn(
                "authentication_failed traceId={} method={} path={} reason=invalid_authorization_scheme",
                request.getAttribute(AuthRequestConstants.TRACE_ID_ATTRIBUTE),
                request.method,
                request.requestURI,
            )
            throw BusinessException(ErrorCode.ACCESS_TOKEN_REQUIRED)
        }

        val accessToken = authorization.removePrefix("Bearer ").trim()
        val claims = authTokenProvider.parseAccessToken(accessToken)
        request.setAttribute(AuthRequestConstants.USER_ID_ATTRIBUTE, claims.userId)
        request.setAttribute(AuthRequestConstants.USER_ROLE_ATTRIBUTE, claims.role)
        return true
    }

    companion object {
        private val log = LoggerFactory.getLogger(AuthenticationInterceptor::class.java)
    }
}
