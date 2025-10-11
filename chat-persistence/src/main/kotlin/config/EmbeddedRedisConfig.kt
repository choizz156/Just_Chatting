package com.chat.persistence.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import redis.embedded.RedisServer
import redis.embedded.core.RedisServerBuilder
import java.io.IOException
import java.net.ServerSocket

@Profile("test || local")
@Configuration
class EmbeddedRedisConfig {
    private val host = "localhost"
    private var port = 0
    private var redisServer: RedisServer? = null

    @Bean
    fun redisConnectionFactory(): LettuceConnectionFactory {
        return LettuceConnectionFactory(host, port)
    }

    @Bean
    fun redisTemplate(connectionFactory: LettuceConnectionFactory): RedisTemplate<String, Any> {
        val template = RedisTemplate<String, Any>()
        template.connectionFactory = connectionFactory
        template.keySerializer = StringRedisSerializer()
        template.valueSerializer = GenericJackson2JsonRedisSerializer(objectMapper())
        template.hashKeySerializer = StringRedisSerializer()
        template.hashValueSerializer = GenericJackson2JsonRedisSerializer(objectMapper())
        return template
    }

    @Bean
    fun redisMessageListenerContainer(connectionFactory: LettuceConnectionFactory): RedisMessageListenerContainer {
        val container = RedisMessageListenerContainer()
        container.setConnectionFactory(connectionFactory)
        return container
    }

    @Bean
    fun objectMapper(): ObjectMapper {
        val mapper = ObjectMapper()
        mapper.registerModule(JavaTimeModule())
        mapper.registerModule(KotlinModule.Builder().build())
        return mapper
    }

    @PostConstruct
    fun redisServer() {
        try {
            val defaultRedisPort = 6380
            port = if (isPortInUse(defaultRedisPort)) findAvailablePort() else defaultRedisPort

            try {
                redisServer = RedisServerBuilder()
                    .port(port)
                    .setting("daemonize no")
                    .setting("appendonly no")
                    .setting("save \" \"")
                    .setting("dbfilename \" \"")
                    .setting("stop-writes-on-bgsave-error no")
                    .build()

                redisServer?.start()
                logger.info("Embedded Redis started on port {}", port)
            } catch (e: Exception) {
                logger.error(
                    "Failed to start embedded Redis server. Tests will continue without Redis. Error: {}",
                    e.message
                )
            }
        } catch (e: Exception) {
            logger.error("Error during Redis server initialization: {}", e.message)
        }
    }

    @PreDestroy
    fun stopRedis() {
        try {
            redisServer?.stop()
            logger.info("Embedded Redis stopped")
        } catch (e: Exception) {
            logger.error("Error stopping Redis server: {}", e.message)
        }
    }

    private fun isPortInUse(port: Int): Boolean {
        return try {
            ServerSocket(port).use { false }
        } catch (e: IOException) {
            true
        }
    }

    private fun findAvailablePort(): Int {
        for (p in 10000..65535) {
            if (!isPortInUse(p)) {
                return p
            }
        }
        throw IllegalArgumentException("No available port found: 10000 ~ 65535")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(EmbeddedRedisConfig::class.java)
    }
}