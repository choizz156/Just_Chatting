package com.chat.persistence.application

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

@Service
class MessageSequenceService(
    private val redisTemplate: RedisTemplate<String, String>
) {
    private val prefix = "chat:sequence"

    fun getNextSequence(chatRoomId: Long): Long{
        val key = "${prefix}:${chatRoomId}"
        return redisTemplate.opsForValue().increment(key) ?: 1L
    }
}




