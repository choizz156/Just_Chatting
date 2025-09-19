package com.chat.api.user

import api.TestApplication
import com.chat.core.application.UserService
import com.chat.core.application.dto.UserDto
import com.chat.core.domain.entity.UserRole
import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.time.LocalDateTime

@ActiveProfiles("test")
@SpringBootTest(
    classes = [TestApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
class UserControllerTest(
    @Autowired
    private val mockMvc: MockMvc,
    @Autowired
    private val objectMapper: ObjectMapper
) {

    @MockBean
    private lateinit var userServiceV1: UserService

    @Test
    fun `join success`() {
        // given
        val request = CreateUserRequest(
            email = "test@test.com",
            password = "password",
            displayName = "testUser"
        )
        val createdUser = UserDto(
            id = 1L,
            email = request.email,
            nickname = request.displayName,
            profileImageUrl = null,
            status = null,
            isActive = true,
            roles = UserRole.USER,
            lastSeenAt = null,
            createdAt = LocalDateTime.now()
        )

        given(userServiceV1.createUser(any())).willReturn(createdUser)

        // when then
        mockMvc.post("/users") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isCreated() }
            jsonPath("$.data") { value(1L) }
            jsonPath("$.time") { value(notNullValue()) }
        }.andDo { print() }
    }

    @Test
    fun `join fail - blank email`() {
        // given
        val request = CreateUserRequest(
            email = "3423mek",
            password = "password",
            displayName = "testUser"
        )

        // when & then
        mockMvc.post("/users") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
        }.andDo { print() }
    }
}
