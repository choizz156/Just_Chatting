package com.chat.persistence.test.repository

import com.chat.core.domain.entity.ChatMessage
import com.chat.persistence.repository.ChatMessageRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import kotlin.test.Test

@ActiveProfiles("test")
@EnableAutoConfiguration
@EnableMongoRepositories(basePackages = ["com.chat.persistence.repository"])
@ContextConfiguration(
    classes = [ChatMessageRepository::class]
)
@DataMongoTest
class ChatMessageRepositoryTest {

    @Autowired
    lateinit var chatMessageRepository: ChatMessageRepository

    var chatMessage1: ChatMessage? = null
    var chatMessage2: ChatMessage? = null
    var chatMessage3: ChatMessage? = null


    @BeforeEach
    fun setUp() {
        chatMessageRepository.deleteAll()

        val chat1 = ChatMessage(
            chatRoomId = 1L,
            senderId = 1L,
            content = "Hi there",
        )
        chatMessage1 = chatMessageRepository.save(chat1)

        val chat2 = ChatMessage(
            chatRoomId = 1L,
            senderId = 2L,
            content = "welcome",
        )
        chatMessage2 = chatMessageRepository.save(chat2)

        val chat3 = ChatMessage(
            chatRoomId = 1L,
            senderId = 1L,
            content = "good",
        )
        chatMessage3 = chatMessageRepository.save(chat3)
    }

    @DisplayName("채팅을 저장할 수 있다")
    @Test
    fun `save message`() {
        //given
        val chatMessage1 = ChatMessage(
            chatRoomId = 1L,
            senderId = 1L,
            content = "there",
        )

        //when
        val result = chatMessageRepository.save(chatMessage1)

        //then
        assertThat(result.content).isEqualTo("there");
    }

    @DisplayName("최신 메시지를 조회할 수 있다.")
    @Test
    fun `find latest message`() {
        //given
        val chatRoomId = 1L
        val pageable = PageRequest.of(0, 10)

        //when
        val findLatestMessages = chatMessageRepository.findLatestMessagesByChatRoomId(chatRoomId, pageable)

        //then
        assertThat(findLatestMessages).hasSize(3)
            .extracting("content")
            .containsExactlyInAnyOrder("good", "welcome", "Hi there")
    }

    @DisplayName("특정 메시지 이전의 메시지를 조회할 수 있다.")
    @Test
    fun `find message before`() {
        //given
        val chatRoomId = 1L
        val pageable = PageRequest.of(0, 10)

        //when
        val findLatestMessages =
            chatMessageRepository.findChatMessagesBefore(chatRoomId, chatMessage2?.id!!, pageable)

        //then
        assertThat(findLatestMessages).hasSize(1)
            .extracting("content")
            .containsExactlyInAnyOrder("Hi there")
    }

    @DisplayName("특정 메시지 이후의 메시지를 조회할 수 있다.")
    @Test
    fun `find message after`() {
        //given
        val chatRoomId = 1L
        val pageable = PageRequest.of(0, 10)

        //when
        val findLatestMessages =
            chatMessageRepository.findChatMessagesAfter(chatRoomId, chatMessage2?.id!!, pageable)

        //then
        assertThat(findLatestMessages).hasSize(1)
            .extracting("content")
            .containsExactlyInAnyOrder("good")
    }
}