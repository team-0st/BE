package com.zerost.api.testerlink.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode

object TesterLinkParser {

    private val DEEP_LINK_PATTERN =
        Regex("""^intoss-private://[^\s?]+\?[^\s]*_deploymentId=([^&\s]+)""")

    fun parse(raw: String): ParsedTesterLink {
        val deepLink = raw.trim()
        val match = DEEP_LINK_PATTERN.find(deepLink)
            ?: throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)

        val deploymentId = match.groupValues[1].trim()
        if (deploymentId.isEmpty()) {
            throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)
        }

        return ParsedTesterLink(
            deepLink = deepLink,
            deploymentId = deploymentId,
        )
    }
}

data class ParsedTesterLink(
    val deepLink: String,
    val deploymentId: String,
)
