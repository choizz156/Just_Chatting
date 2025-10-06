package com.chat.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

@SpringBootApplication(
    scanBasePackages = [
        "com.chat.app",
        "com.chat.core",
        "com.chat.persistence",
        "com.chat.auth",
        "com.chat.api",
        "com.chat.websocket",
    ]
)
@EntityScan(basePackages = ["com.chat.core.domain.entity"])
@EnableMongoRepositories(basePackages = ["com.chat.persistence.repository"])
class JustChatApplication

fun main(args: Array<String>) {
    runApplication<JustChatApplication>(*args)
}