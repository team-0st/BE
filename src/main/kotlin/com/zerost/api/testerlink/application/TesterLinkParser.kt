package com.zerost.api.testerlink.application

import com.zerost.api.common.exception.BusinessException
import com.zerost.api.common.exception.ErrorCode

object TesterLinkParser {

    private const val SCHEME_PREFIX = "intoss-private://"
    private const val DEPLOYMENT_ID_KEY = "_deploymentId"

    fun parse(raw: String): ParsedTesterLink {
        val deepLink = raw.trim()
        if (deepLink.isEmpty() || deepLink.any { it.isWhitespace() }) {
            throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)
        }
        if (!deepLink.startsWith(SCHEME_PREFIX)) {
            throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)
        }

        val queryStart = deepLink.indexOf('?')
        if (queryStart <= SCHEME_PREFIX.length) {
            throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)
        }

        val authorityAndPath = deepLink.substring(SCHEME_PREFIX.length, queryStart)
        if (authorityAndPath.isBlank() || authorityAndPath.any { it == '?' || it == '#' }) {
            throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)
        }

        val query = deepLink.substring(queryStart + 1)
        if (query.isEmpty() || query.contains('#')) {
            throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)
        }

        val deploymentId = extractDeploymentId(query)
            ?: throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)
        if (deploymentId.isEmpty() || deploymentId.any { it.isWhitespace() }) {
            throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)
        }

        return ParsedTesterLink(
            deepLink = deepLink,
            deploymentId = deploymentId,
        )
    }

    /** 쿼리 키 `_deploymentId`만 인정. 다른 파라미터 값에 포함된 동일 문자열은 무시. */
    private fun extractDeploymentId(query: String): String? {
        for (part in query.split('&')) {
            if (part.isEmpty()) {
                continue
            }
            val eq = part.indexOf('=')
            if (eq <= 0) {
                continue
            }
            val key = part.substring(0, eq)
            if (key == DEPLOYMENT_ID_KEY) {
                return part.substring(eq + 1)
            }
        }
        return null
    }
}

data class ParsedTesterLink(
    val deepLink: String,
    val deploymentId: String,
)
