package com.chat.api.user

import com.chat.api.TestApplication
import com.chat.core.application.UserService
import com.chat.core.application.dto.UserDto
import com.chat.core.domain.entity.UserRole
import com.fasterxml.jackson.databind.ObjectMapper
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.time.Instant
import kotlin.test.Test


@WebMvcTest(UserController::class)
@Import(
    TestApplication::class
)
class UserControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper,
) {

    @MockBean
    private lateinit var userService: UserService

    @Test
    fun `회원가입 성공`() {
        // given
        val request = CreateUserRequest(
            email = "test@test.com",
            password = "password123",
            nickname = "testUser"
        )

        val createdUser = UserDto(
            id = "1",
            email = request.email,
            nickname = request.nickname,
            profileImage = null,
            isActive = true,
            roles = UserRole.USER,
            lastSeenAt = Instant.now(),
            createdAt = Instant.now()
        )

        given(userService.createUser(any())).willReturn(createdUser)

        // when & then
        mockMvc.post("/users") {
            contentType = MediaType.MULTIPART_FORM_DATA
            param("email", request.email)
            param("password", request.password)
            param("nickname", request.nickname)
        }.andExpect {
            status { isCreated() }
            jsonPath("$.data.id") { value("1") }
            jsonPath("$.time") { exists() }
        }.andDo { print() }
    }

    @Test
    fun `이메일 형식이 잘못된 경우 BadRequest`() {
        // given
        val invalidRequest = CreateUserRequest(
            email = "invalidEmail",
            password = "password123",
            nickname = "testUser"
        )

        // when & then
        mockMvc.post("/users") {
            contentType = MediaType.MULTIPART_FORM_DATA
            param("email", invalidRequest.email)
            param("password", invalidRequest.password)
            param("nickname", invalidRequest.nickname)
        }.andExpect {
            status { isBadRequest() }
        }.andDo { print() }
    }

    @Test
    fun `이메일에 공백이 포함된 경우 BadRequest`() {
        // given
        val invalidRequest = CreateUserRequest(
            email = "test @test.com",
            password = "password123",
            nickname = "testUser"
        )

        // when & then
        mockMvc.post("/users") {
            contentType = MediaType.MULTIPART_FORM_DATA
            param("email", invalidRequest.email)
            param("password", invalidRequest.password)
            param("nickname", invalidRequest.nickname)
        }.andExpect {
            status { isBadRequest() }
        }.andDo { print() }
    }
}