package com.chat.persistence.test

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@SpringBootApplication(scanBasePackages = ["com.chat"])
@EntityScan(basePackages = ["com.chat.core.domain.entity"])
@EnableJpaRepositories(basePackages = ["com.chat.persistence.repository"])
class TestApplication {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder()
    }
}