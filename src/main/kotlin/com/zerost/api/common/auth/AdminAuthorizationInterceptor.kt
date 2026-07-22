package com.zerost.api.common.auth

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.user.domain.UserRole
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class AdminAuthorizationInterceptor : HandlerInterceptor {

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        val role = request.getAttribute(AuthRequestConstants.USER_ROLE_ATTRIBUTE) as? UserRole
        if (role == UserRole.ADMIN) {
            return true
        }

        throw BusinessException(ErrorCode.ADMIN_ACCESS_DENIED)
    }
}
