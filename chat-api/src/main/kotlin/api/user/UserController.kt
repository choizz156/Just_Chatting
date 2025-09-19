package com.chat.api.user

import api.ApiResponseDto
import com.chat.core.application.UserService
import com.chat.core.application.dto.CreateUserContext
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService
) {

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    fun join(@RequestBody joinDto: @Valid CreateUserRequest): ApiResponseDto<Long> {

        val joinContext = toContext(joinDto)
        val user = userService.createUser(joinContext)

        return ApiResponseDto(user.id)
    }

    fun toContext(request: CreateUserRequest): CreateUserContext {
        return CreateUserContext(request.email, request.password, request.displayName)
    }
}