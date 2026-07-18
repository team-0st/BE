package com.zerost.api.common.device

import com.zerost.api.common.device.DeviceConstants.DEVICE_ID_ATTRIBUTE
import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class DeviceIdInterceptor : HandlerInterceptor {

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ) : Boolean {
        if (request.method == "OPTIONS") {
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
