package application

import com.chat.core.application.UserService
import com.chat.core.application.dto.CreateUserContext
import com.chat.core.domain.entity.UserRole
import com.chat.persistence.repository.UserRepository
import com.chat.persistence.test.TestApplication
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import kotlin.test.Test

@ActiveProfiles("test")
@SpringBootTest(classes = [TestApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserServiceV1Test(
    @Autowired val userServiceV1: UserService,
    @Autowired val userRepository: UserRepository
) {

    @BeforeEach
    fun setUp() {
        userRepository.deleteAllInBatch()
    }

    @Test
    fun `create user`() {
        val createUserContext = CreateUserContext("test@gmail.com", "dsfsdf123", "test11")

        val createUser = userServiceV1.createUser(createUserContext)

        assertThat(userRepository.count()).isEqualTo(1)
        assertThat(createUser.email).isEqualTo(createUserContext.email)
        assertThat(createUser.nickname).isEqualTo(createUserContext.nickname)
        assertThat(createUser.roles).isEqualTo(UserRole.USER)
    }
}