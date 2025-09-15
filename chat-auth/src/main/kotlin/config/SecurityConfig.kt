package com.chat.auth.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain


@EnableWebSecurity
@Configuration
class SecurityConfig(
    private val objectMapper: ObjectMapper,
    private val passwordEncoder: PasswordEncoder
) {

    @Bean
    fun webSecurityCustomizer(): WebSecurityCustomizer {
        return WebSecurityCustomizer { web ->
            web.ignoring().requestMatchers("/profile")
        }
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http.csrf { it.disable() }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .cors { cors -> cors.configurationSource(CorsConfig().corsFilter()) }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                    .sessionFixation()
                    .changeSessionId()
                    .maximumSessions(1)
                    .maxSessionsPreventsLogin(false)
                    .expiredUrl("/login")
            }
            .authorizeHttpRequests {
                it.requestMatchers(HttpMethod.GET, "/auth/**").hasAnyRole("USER")
                it.requestMatchers("/chat-rooms", "/messages").hasAnyRole("USER")
                it.anyRequest().permitAll()
            }

            .build()
    }


    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder()
    }
}


