package com.chat.api

import java.time.Instant

data class ApiResponseDto<T>(val result: T, val time: Instant = Instant.now()) {
    companion object {
        fun <T> to(result: T): ApiResponseDto<T> {
            return ApiResponseDto(result)
        }
    }
}
