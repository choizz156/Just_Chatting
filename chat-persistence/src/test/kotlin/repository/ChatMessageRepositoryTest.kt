package com.chat.persistence.test.repository

import com.chat.core.domain.entity.*
import com.chat.persistence.repository.ChatMessageRepository
import com.chat.persistence.repository.ChatRoomRepository
import com.chat.persistence.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
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
    classes = [ChatMessageRepository::class, ChatRoomRepository::class, UserRepository::class]
)
@DataMongoTest
class ChatMessageRepositoryTest {
    @Autowired
    private lateinit var chatMessageRepository: ChatMessageRepository

    @Autowired
    private lateinit var chatRoomRepository: ChatRoomRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    private lateinit var testUser: User
    private lateinit var testChatRoom: ChatRoom

    @BeforeEach
    fun setUp() {
        chatMessageRepository.deleteAll()
        chatRoomRepository.deleteAll()
        userRepository.deleteAll()

        testUser = userRepository.save(
            User(
                email = "test@test.com",
                password = "password",
                nickname = "testUser",
                profileImageUrl = null
            )
        )

        testChatRoom = chatRoomRepository.save(ChatRoom(
            name = "test1",
            type = ChatRoomType.GROUP,
            createdBy = testUser.id
        ))
    }

    @DisplayName("채팅 메시지를 저장하고 조회할 수 있다.")
    @Test
    fun `save and find chat message`() {
        // given
        val chatMessage = ChatMessage(
            chatRoomId = testChatRoom.id.toString(),
            sender = MessageSender(testUser.id.toString(), testUser.nickname, null),
            content = "hello"
        )
        chatMessageRepository.save(chatMessage)

        // when
        val messages = chatMessageRepository.findByChatRoomIdOrderByIdDesc(testChatRoom.id.toString(), PageRequest.of(0, 10))

        // then
        assertThat(messages.content).hasSize(1)
        assertThat(messages.content[0].content).isEqualTo("hello")
    }

    @DisplayName("채팅방의 마지막 메시지를 조회할 수 있다.")
    @Test
    fun `find top message`() {
        // given
        val chatMessage1 = ChatMessage(
            chatRoomId = testChatRoom.id.toString(),
            sender = MessageSender(testUser.id.toString(), testUser.nickname, null),
            content = "hello"
        )
        chatMessageRepository.save(chatMessage1)

        val chatMessage2 = ChatMessage(
            chatRoomId = testChatRoom.id.toString(),
            sender = MessageSender(testUser.id.toString(), testUser.nickname, null),
            content = "world"
        )
        chatMessageRepository.save(chatMessage2)

        // when
        val topMessage = chatMessageRepository.findTopByChatRoomIdOrderByIdDesc(testChatRoom.id.toString())

        // then
        assertThat(topMessage).isNotNull
        assertThat(topMessage!!.content).isEqualTo("world")
    }
}