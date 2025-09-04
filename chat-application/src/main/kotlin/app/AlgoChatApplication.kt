package com.chat.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(
    scanBasePackages = [
        "com.chat.app",
        "com.chat.core",
        "com.chat.persistence"
    ]
)
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = ["com.chat.persistence.repository"])
@EntityScan(basePackages = ["com.chat.domain.domain.entity"])
class AlgoChatApplication

fun main(args: Array<String>) {
    runApplication<AlgoChatApplication>(*args)
}