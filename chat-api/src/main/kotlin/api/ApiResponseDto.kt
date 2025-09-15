package api

import java.time.LocalDateTime

data class ApiResponseDto<T>(val data: T, val time: LocalDateTime = LocalDateTime.now())