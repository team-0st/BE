package com.zerost.api.common.auth

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import com.zerost.api.user.domain.UserRole
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class AdminAuthorizationInterceptor : HandlerInterceptor {

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        val userId = request.getAttribute(AuthRequestConstants.USER_ID_ATTRIBUTE) as? Long
        val role = request.getAttribute(AuthRequestConstants.USER_ROLE_ATTRIBUTE) as? UserRole
        if (role == UserRole.ADMIN) {
            log.info(
                "admin_access_granted traceId={} adminUserId={} method={} path={}",
                request.getAttribute(AuthRequestConstants.TRACE_ID_ATTRIBUTE),
                userId,
                request.method,
                request.requestURI,
            )
            return true
        }

        log.warn(
            "admin_access_denied traceId={} userId={} role={} method={} path={}",
            request.getAttribute(AuthRequestConstants.TRACE_ID_ATTRIBUTE),
            userId,
            role,
            request.method,
            request.requestURI,
        )
        throw BusinessException(ErrorCode.ADMIN_ACCESS_DENIED)
    }

    companion object {
        private val log = LoggerFactory.getLogger(AdminAuthorizationInterceptor::class.java)
    }
}
