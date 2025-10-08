package com.chat.persistence.redis

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class OnlineUsers(
    private val redisTemplate: RedisTemplate<String, OnlineUserDto>,
    private val redisMessageBroker: RedisMessageBroker
) {
    private val onlineUsers = redisTemplate.opsForHash<String, OnlineUserDto>()
    private val ONLINE_USERS_KEY = "online.users"

    fun add(user: OnlineUserDto) {
        onlineUsers.put(ONLINE_USERS_KEY, user.id, user)
    }

    fun loadOnlineUsers() {
        redisMessageBroker.subscribeOnlineUsers()
    }

    fun remove(userId: String) {
        val isContain = onlineUsers.hasKey(ONLINE_USERS_KEY, userId) == true
        if (isContain) {
            onlineUsers.delete(ONLINE_USERS_KEY, userId)
        }
    }

    fun broadcast() {
        val onlineUserList = getOnlineUsers()
        redisMessageBroker.broadcastOnlineUsers(onlineUserList)
    }

    private fun getOnlineUsers(): List<OnlineUserDto> {
        return onlineUsers.values(ONLINE_USERS_KEY)
    }
}

data class OnlineUserDto(
    val id: String,
    val nickname: String,
    val profileImage: String,
)
