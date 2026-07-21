package com.zerost.api.common.device

import com.zerost.api.auth.application.AuthTokenProvider
import com.zerost.api.common.device.DeviceConstants.DEVICE_ID_ATTRIBUTE
import com.zerost.api.common.device.DeviceConstants.USER_ID_ATTRIBUTE
import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class DeviceIdInterceptor(
    private val authTokenProvider: AuthTokenProvider,
) : HandlerInterceptor {

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ) : Boolean {
        val authorization = request.getHeader("Authorization")?.trim()
        if (!authorization.isNullOrEmpty() && authorization.startsWith("Bearer ")) {
            val accessToken = authorization.removePrefix("Bearer ").trim()
            val claims = authTokenProvider.parseAccessToken(accessToken)
            request.setAttribute(DEVICE_ID_ATTRIBUTE, claims.deviceId)
            request.setAttribute(USER_ID_ATTRIBUTE, claims.userId)
            return true
        }

        val deviceId = request.getHeader(DeviceConstants.DEVICE_ID_HEADER)?.trim()

        if (deviceId.isNullOrEmpty()) {
            throw BusinessException(ErrorCode.DEVICE_ID_HEADER_MISSING)
        }

        request.setAttribute(DeviceConstants.DEVICE_ID_ATTRIBUTE, deviceId)
        return true
    }
}
