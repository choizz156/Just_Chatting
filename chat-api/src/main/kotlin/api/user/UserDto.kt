package api.user

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateUserRequest(

    @NotSpace
    @field:NotBlank(message = "이메일은 필수입니다")
    @field:Email(message = "@를 포함해야합니다.")
    val email: String,

    @NotSpace
    @field:NotBlank(message = "비밀번호는 필수입니다")
    @field:Size(min = 3, message = "비밀번호는 최소 3자 이상이어야 합니다")
    val password: String,

    @NotSpace
    @field:NotBlank(message = "표시 이름은 필수입니다")
    @field:Size(min = 1, max = 50, message = "표시 이름은 1-50자 사이여야 합니다")
    val displayName: String
)

data class LoginRequest(
    @field:NotBlank(message = "사용자명은 필수입니다")
    val username: String,

    @field:NotBlank(message = "비밀번호는 필수입니다")
    val password: String
)
