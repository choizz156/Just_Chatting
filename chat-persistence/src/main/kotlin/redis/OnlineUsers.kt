package com.chat.persistence.redis

import com.chat.core.application.dto.OnlineUserDto
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class OnlineUsers(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val redisMessageBroker: RedisMessageBroker
) {
    private val ONLINE_USERS_KEY = "online.users"

    fun add(user: OnlineUserDto) {
        redisTemplate.opsForHash<String, OnlineUserDto>().put(ONLINE_USERS_KEY, user.id, user)
    }

    fun loadOnlineUsers() {
        redisMessageBroker.subscribeOnlineUsers()
    }

    fun remove(userId: String) {
        val isContain =  redisTemplate.opsForHash<String, OnlineUserDto>().hasKey(ONLINE_USERS_KEY, userId) == true
        if (isContain) {
            redisTemplate.opsForHash<String, OnlineUserDto>().delete(ONLINE_USERS_KEY, userId)
        }
    }

    fun broadcast() {
        val onlineUserList = getOnlineUsers()
        redisMessageBroker.broadcastOnlineUsers(onlineUserList)
    }

    private fun getOnlineUsers(): List<OnlineUserDto> {
        return  redisTemplate.opsForHash<String, OnlineUserDto>().values(ONLINE_USERS_KEY)
    }
}


