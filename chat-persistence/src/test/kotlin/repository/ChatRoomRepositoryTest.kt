package com.chat.persistence.test.repository

import com.chat.core.domain.entity.ChatRoom
import com.chat.core.domain.entity.ChatRoomMember
import com.chat.core.domain.entity.ChatRoomType
import com.chat.core.domain.entity.MemberRole
import com.chat.core.domain.entity.User
import com.chat.persistence.repository.ChatRoomMemberRepository
import com.chat.persistence.repository.ChatRoomRepository
import com.chat.persistence.repository.UserRepository
import com.chat.persistence.repository.findByIdOrThrow
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration

@ActiveProfiles("test")
@EnableAutoConfiguration
@EnableMongoRepositories(basePackages = ["com.chat.persistence.repository"])
@ContextConfiguration(
    classes = [ChatRoomRepository::class, ChatRoomMemberRepository::class, UserRepository::class]
)
@DataMongoTest
class ChatRoomRepositoryTest {
    @Autowired
    private lateinit var chatRoomRepository: ChatRoomRepository

    @Autowired
    private lateinit var chatRoomMemberRepository: ChatRoomMemberRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    private lateinit var testUser: User

    @BeforeEach
    fun setUp() {
        chatRoomRepository.deleteAll()
        chatRoomMemberRepository.deleteAll()
        userRepository.deleteAll()

        testUser = userRepository.save(
            User(
                email = "test@test.com",
                password = "password",
                nickname = "testUser",
                profileImage = null
            )
        )

        val chatRoom1 = chatRoomRepository.save(ChatRoom(
            name = "test1",
            type = ChatRoomType.GROUP,
            createdBy = testUser
        ))

        val chatRoom2 = chatRoomRepository.save(ChatRoom(
            name = "test2",
            type = ChatRoomType.GROUP,
            createdBy = testUser
        ))

        val chatRoomMember1 = ChatRoomMember(
            userId = testUser.id,
            chatRoomId= chatRoom1.id,
            role = MemberRole.MEMBER
        )
        val chatRoomMember2 = ChatRoomMember(
            userId = testUser.id,
            chatRoomId = chatRoom2.id,
            role = MemberRole.MEMBER
        )

        chatRoomMemberRepository.save(chatRoomMember1)
        chatRoomMemberRepository.save(chatRoomMember2)
    }

    @DisplayName("유저가 속한 채팅방을 조회할 수 있다.")
    @Test
    fun `find chatroom by user id`() {

        //when
        val result =
            chatRoomRepository.findChatRoomsByUserId(testUser.id, PageRequest.of(0, 10))

        //then
        assertThat(result).hasSize(2)
            .extracting("name")
            .contains("test1", "test2")
    }

    @DisplayName("활성화된 채팅방을 조회할 수 있다.")
    @Test
    fun `find chatRoom`() {
        //given
        val chatRoom3Dto = ChatRoom(
            name = "test3",
            type = ChatRoomType.GROUP,
            createdBy = testUser
        )
        chatRoomRepository.save(chatRoom3Dto)

        //when
        val result =
            chatRoomRepository.findByIsActiveTrueOrderByIdDesc()

        //then
        assertThat(result).hasSize(3)
            .extracting("name")
            .contains("test1", "test2", "test3")
    }

    @DisplayName("채팅방을 검색할 수 있다.")
    @Test
    fun `search chatRoom`() {
        //given
        val chatRoom3Dto = ChatRoom(
            name = "테스트 채팅방",
            type = ChatRoomType.GROUP,
            createdBy = testUser
        )
        chatRoomRepository.save(chatRoom3Dto)

        //when
        val result =
            chatRoomRepository.findByNameContainingIgnoreCaseAndIsActiveTrueOrderByIdDesc("테스트")

        //then
        assertThat(result).hasSize(1)
            .extracting("name")
            .contains("테스트 채팅방")
    }

    @DisplayName("채팅방 id로 채팅방을 조회할 수 있다.")
    @Test
    fun `exist room`() {
        //given
        val chatRoom3Dto = ChatRoom(
            name = "테스트 채팅방",
            type = ChatRoomType.GROUP,
            createdBy = testUser
        )
        val chatRoom = chatRoomRepository.save(chatRoom3Dto)

        //when
        val result =
            chatRoomRepository.findByIdOrThrow(chatRoom.id.toString())

        //then
        assertThat(result.id).isEqualTo(chatRoom.id)
    }

    @DisplayName("채팅방이 존재하지 않으면 예외를 던진다.")
    @Test
    fun `no exist room`() {
        //given

        //when then
        assertThatThrownBy { chatRoomRepository.findByIdOrThrow("noExist") }
            .isInstanceOf(IllegalArgumentException::class.java)
    }
}