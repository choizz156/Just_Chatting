package com.chat.persistence.redis

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class OnlineUsers(
    private val redisTemplate: RedisTemplate<String, OnlineUserDto>
) {

    private val onlineUserSet = redisTemplate.opsForSet()

    fun add(user: OnlineUserDto) {
        onlineUserSet.add(user.id, user)
    }

    fun remove(userId: String) {
        onlineUserSet.remove(userId)
    }
}

data class OnlineUserDto(
    val id: String,
    val nickname: String,
    val profileImage: String,
)
