package com.zerost.api.common.auth

import com.zerost.api.auth.application.AuthTokenProvider
import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
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
        if (!authorization.isNullOrEmpty() && authorization.startsWith("Bearer ")) {
            val accessToken = authorization.removePrefix("Bearer ").trim()
            val claims = authTokenProvider.parseAccessToken(accessToken)
            request.setAttribute(AuthRequestConstants.USER_ID_ATTRIBUTE, claims.userId)
            request.setAttribute(AuthRequestConstants.USER_ROLE_ATTRIBUTE, claims.role)
            return true
        }

        throw BusinessException(ErrorCode.ACCESS_TOKEN_REQUIRED)
    }
}
