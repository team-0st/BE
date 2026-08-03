package com.zerost.api.common.response

data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val error: ApiErrorResponse?,
) {
    companion object {
        fun <T> success(data: T?): ApiResponse<T> =
            ApiResponse(
                success = true,
                data = data,
                error = null,
            )

        fun success(): ApiResponse<Unit> =
            ApiResponse(
                success = true,
                data = Unit,
                error = null,
            )

        fun failure(error: ApiErrorResponse): ApiResponse<Nothing> =
            ApiResponse(
                success = false,
                data = null,
                error = error,
            )
    }
}
