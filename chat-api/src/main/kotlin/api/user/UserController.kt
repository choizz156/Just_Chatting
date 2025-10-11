package com.chat.api.user

import com.chat.api.ApiResponseDto
import com.chat.core.application.UserService
import com.chat.core.application.dto.CreateUserContext
import com.chat.core.application.dto.UserDto
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService,
) {

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    fun join(@Valid @RequestBody joinDto: CreateUserRequest): ApiResponseDto<UserDto> {

        val joinContext = toContext(joinDto)
        val userDto = userService.createUser(joinContext)

        return ApiResponseDto(userDto)
    }

    @GetMapping
    fun getOnlineUsers(){

    }


    fun toContext(request: CreateUserRequest): CreateUserContext {
        return CreateUserContext(request.email, request.password, request.nickname, request.profileImage)
    }
}