package com.zerost.api.testerlink.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode
import java.net.URI

object TossShareUrlNormalizer {

    private const val MAX_LENGTH = 2048

    fun normalize(raw: String): String {
        val url = raw.trim()
        if (url.length > MAX_LENGTH) {
            throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)
        }

        val uri = runCatching { URI(url) }.getOrNull()
            ?: throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)

        if (!uri.isAbsolute || uri.scheme != "https" || uri.host.isNullOrBlank()) {
            throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)
        }

        return url
    }
}
