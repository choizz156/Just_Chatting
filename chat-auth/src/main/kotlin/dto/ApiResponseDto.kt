package com.chat.auth.dto

import java.time.LocalDateTime

data class ApiResponseDto<T>(val result: T, val time: LocalDateTime = LocalDateTime.now())