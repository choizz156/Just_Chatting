package com.chat.persistence.application

import com.chat.core.domain.entity.Message
import com.chat.persistence.repository.MessageRepository
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

@Service
class MessageService(
    private val redisTemplate: RedisTemplate<String, String>,
    private val messageRepository: MessageRepository,
) {
    private val prefix = "chat:sequence"

    fun getNextSequence(chatRoomId: Long): Long{
        val key = "${prefix}:${chatRoomId}"
        return redisTemplate.opsForValue().increment(key) ?: 1L
    }

    fun saveMessage(message: Message): Message {
        return messageRepository.save(message)
    }
}




