package com.chat.persistence.test.repository

import com.chat.core.domain.entity.ChatMessage
import com.chat.persistence.repository.ChatMessageRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import kotlin.test.Test

@ActiveProfiles("test")
@EnableAutoConfiguration
@EnableMongoRepositories(basePackages = ["com.chat.persistence"])
@ContextConfiguration(
    classes = [ChatMessageRepository::class]
)
@DataMongoTest
class ChatMessageRepositoryTest {

    @Autowired
    lateinit var chatMessageRepository: ChatMessageRepository

    @Test
    fun `save message`() {
        //given
        val chatMessage1 = ChatMessage(
            chatRoomId = 1L,
            senderId = 1L,
            content = "Hi there",
        )

        //when
        chatMessageRepository.save(chatMessage1)

        //then
        val result = chatMessageRepository.findAll();

        assertThat(result.get(0).content).isEqualTo("Hi there");
    }
}