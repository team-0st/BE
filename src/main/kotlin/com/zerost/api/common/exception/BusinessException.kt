package com.zerost.api.common.exception

class BusinessException(
    val errorCode: ErrorCode,
) : RuntimeException(errorCode.message)
