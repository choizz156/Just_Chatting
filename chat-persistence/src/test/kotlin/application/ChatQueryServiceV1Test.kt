package com.chat.persistence.test.application

import com.chat.core.application.ChatQueryService
import com.chat.core.domain.entity.ChatRoom
import com.chat.core.domain.entity.ChatRoomMember
import com.chat.core.domain.entity.User
import com.chat.core.dto.ChatRoomDto
import com.chat.persistence.repository.ChatRoomMemberRepository
import com.chat.persistence.repository.ChatRoomRepository
import com.chat.persistence.repository.UserRepository
import com.chat.persistence.test.TestApplication
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@SpringBootTest(
    classes = [TestApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class ChatQueryServiceV1Test(
    @Autowired val chatQueryService: ChatQueryService,
    @Autowired val chatRoomRepository: ChatRoomRepository,
    @Autowired val chatRoomMemberRepository: ChatRoomMemberRepository,
    @Autowired val userRepository: UserRepository,
) {

    @BeforeEach
    fun setUp() {
        chatRoomRepository.deleteAllInBatch()
        userRepository.deleteAllInBatch()
    }

    @Test
    @DisplayName("채팅방을 조회할 수 있다.")
    fun `find room`() {
        //given
        val user = userRepository.save(
            User(
                email = "test@test.com",
                password = "dfimen1",
                nickname = "test"
            )
        )

        val entity = ChatRoom(
            name = "testRoom",
            createdBy = user
        )
        val chatRoom = chatRoomRepository.save(entity)

        //when
        val result = chatQueryService.getChatRoom(chatRoom.id)

        //then
        assertThat(result.id).isEqualTo(chatRoom.id)
        assertThat(result.name).isEqualTo(chatRoom.name)
    }

    @Test
    @DisplayName("자신이 속한 채팅방 목록만 조회한다")
    fun `find rooms for a specific user`() {
        //given
        val user1 = userRepository.save(User(email = "user1@test.com", password = "p", nickname = "user1"))
        val user2 = userRepository.save(User(email = "user2@test.com", password = "p", nickname = "user2"))

        val room1 = chatRoomRepository.save(ChatRoom(name = "room1", createdBy = user1))
        val room2 = chatRoomRepository.save(ChatRoom(name = "room2", createdBy = user1))
        val room3 = chatRoomRepository.save(ChatRoom(name = "room3", createdBy = user2))
        val room4 = chatRoomRepository.save(ChatRoom(name = "room4", createdBy = user2))


        chatRoomMemberRepository.saveAll(
            listOf(
                ChatRoomMember(chatRoom = room1, user = user1),
                ChatRoomMember(chatRoom = room2, user = user1),
            )
        )

        chatRoomMemberRepository.saveAll(
            listOf(
                ChatRoomMember(chatRoom = room3, user = user2),
                ChatRoomMember(chatRoom = room4, user = user2)
            )
        )

        //when
        val result1: Page<ChatRoomDto> = chatQueryService.getChatRooms(user1.id, PageRequest.of(0, 10))
        val result2: Page<ChatRoomDto> = chatQueryService.getChatRooms(user2.id, PageRequest.of(0, 10))

        //then
        assertThat(result1.content).hasSize(2)
        assertThat(result2.content).hasSize(2)

        assertThat(result1.content.map { it.name }).containsExactlyInAnyOrder(
            "room1",
            "room2",

        )

        assertThat(result2.content.map { it.name }).containsExactlyInAnyOrder(
            "room3",
            "room4"
        )
    }
}