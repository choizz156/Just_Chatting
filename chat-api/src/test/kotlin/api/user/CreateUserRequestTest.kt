package com.chat.api.user

import jakarta.validation.Validation
import jakarta.validation.Validator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class CreateUserRequestTest {

    private lateinit var validator: Validator

    @BeforeEach
    fun setUp() {
        val validatorFactory = Validation.buildDefaultValidatorFactory()
        validator = validatorFactory.validator
    }

    @Test
    @DisplayName("유효한 CreateUserRequest")
    fun `valid CreateUserRequest`() {
        val request = CreateUserRequest(
            email = "test@example.com",
            password = "password123",
            nickname = "TestUser"
        )
        val violations = validator.validate(request)
        assertThat(violations).isEmpty()
    }

    @Test
    @DisplayName("이메일 공백 실패")
    fun `email blank fail`() {
        val request = CreateUserRequest(
            email = "",
            password = "password123",
            nickname = "TestUser"
        )
        val violations = validator.validate(request)
        assertThat(violations).extracting("message").containsAnyOf("이메일은 필수입니다", "글자에 공백이 있습니다.")
    }

    @Test
    @DisplayName("이메일 형식 실패")
    fun `email format fail`() {
        val request = CreateUserRequest(
            email = "invalid-email",
            password = "password123",
            nickname = "TestUser"
        )
        val violations = validator.validate(request)
        assertThat(violations).extracting("message").containsAnyOf("@를 포함해야합니다.")
    }

    @Test
    @DisplayName("비밀번호 공백 실패")
    fun `password blank fail`() {
        val request = CreateUserRequest(
            email = "test@example.com",
            password = "",
            nickname = "TestUser"
        )
        val violations = validator.validate(request)
        assertThat(violations).extracting("message").containsAnyOf("비밀번호는 필수입니다", "비밀번호는 최소 3자 이상이어야 합니다", "글자에 공백이 있습니다.")
    }

    @Test
    @DisplayName("비밀번호 길이 실패")
    fun `password size fail`() {
        val request = CreateUserRequest(
            email = "test@example.com",
            password = "12",
            nickname = "TestUser"
        )
        val violations = validator.validate(request)
        assertThat(violations).extracting("message").containsAnyOf("비밀번호는 최소 3자 이상이어야 합니다")
    }

    @Test
    @DisplayName("닉네임 공백 실패")
    fun `nickname blank fail`() {
        val request = CreateUserRequest(
            email = "test@example.com",
            password = "password123",
            nickname = ""
        )
        val violations = validator.validate(request)
        assertThat(violations).extracting("message").containsAnyOf("닉네임은 필수입니다", "글자에 공백이 있습니다.")
    }

    @Test
    @DisplayName("닉네임 길이 실패")
    fun `nickname size fail`() {
        val request = CreateUserRequest(
            email = "test@example.com",
            password = "password123",
            nickname = "a".repeat(51)
        )
        val violations = validator.validate(request)
        assertThat(violations).extracting("message").containsAnyOf("표시 이름은 1-50자 사이여야 합니다")
    }

    @Test
    @DisplayName("이메일에 공백 포함 실패")
    fun `email contains space fail`() {
        val request = CreateUserRequest(
            email = "test @example.com",
            password = "password123",
            nickname = "TestUser"
        )
        val violations = validator.validate(request)
        assertThat(violations).extracting("message").containsAnyOf("글자에 공백이 있습니다.")
    }

    @Test
    @DisplayName("비밀번호에 공백 포함 실패")
    fun `password contains space fail`() {
        val request = CreateUserRequest(
            email = "test@example.com",
            password = "pass word",
            nickname = "TestUser"
        )
        val violations = validator.validate(request)
        assertThat(violations).extracting("message").containsAnyOf("글자에 공백이 있습니다.")
    }

    @Test
    @DisplayName("닉네임에 공백 포함 실패")
    fun `nickname contains space fail`() {
        val request = CreateUserRequest(
            email = "test@example.com",
            password = "password123",
            nickname = "Test User"
        )
        val violations = validator.validate(request)
        assertThat(violations).extracting("message").containsAnyOf("글자에 공백이 있습니다.")
    }
}