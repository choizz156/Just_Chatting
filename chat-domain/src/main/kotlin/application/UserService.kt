package application

import com.chat.domain.dto.CreateUserRequest
import com.chat.domain.dto.LoginRequest
import com.chat.domain.dto.UserDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface UserService {

    fun createUser(request: CreateUserRequest): UserDto
    fun login(request: LoginRequest): UserDto
    fun getUserById(userId: Long): UserDto
    fun searchUsers(query: String, pageable: Pageable): Page<UserDto>

    fun updateLastSeen(userId: Long): UserDto
}