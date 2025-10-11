package com.chat.auth.config

import com.chat.auth.application.UserDetailsVerification
import com.chat.auth.filter.EmailPasswordAuthFilter
import com.chat.auth.handler.AuthDeniedHandler
import com.chat.auth.handler.AuthEntryPointHandler
import com.chat.auth.handler.AuthFailureHandler
import com.chat.auth.handler.AuthSuccessHandler
import com.chat.persistence.redis.OnlineUsers
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.session.SessionRegistry
import org.springframework.security.core.session.SessionRegistryImpl
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.authentication.session.*
import org.springframework.security.web.context.HttpSessionSecurityContextRepository


@EnableWebSecurity
@Configuration
class SecurityConfig(
    private val objectMapper: ObjectMapper,
    private val userDetailsVerification: UserDetailsVerification,
    private val onlineUsers: OnlineUsers
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
                it.sessionAuthenticationStrategy(sessionAuthenticationStrategy())
                    .sessionFixation()
                    .changeSessionId()
                    .maximumSessions(1)
                    .maxSessionsPreventsLogin(false)
                    .expiredUrl("http://localhost:5173/")
            }
            .addFilterBefore(
                emailPasswordFilter(),
                UsernamePasswordAuthenticationFilter::class.java
            )
            .exceptionHandling {
                it.accessDeniedHandler(AuthDeniedHandler(objectMapper))
                it.authenticationEntryPoint(AuthEntryPointHandler(objectMapper))
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

    @Bean
    fun emailPasswordFilter(): EmailPasswordAuthFilter {
        val filter = EmailPasswordAuthFilter("/auth/login", objectMapper)

        filter.setAuthenticationManager(authenticationManager())
        filter.setSessionAuthenticationStrategy(sessionAuthenticationStrategy())
        filter.setAuthenticationFailureHandler(AuthFailureHandler(objectMapper))
        filter.setAuthenticationSuccessHandler(AuthSuccessHandler(objectMapper, onlineUsers))
        filter.setSecurityContextRepository(HttpSessionSecurityContextRepository())

        return filter
    }

    @Bean
    fun sessionAuthenticationStrategy(): SessionAuthenticationStrategy {
        val concurrentSessionControlStrategy =
            ConcurrentSessionControlAuthenticationStrategy(sessionRegistry())

        concurrentSessionControlStrategy.setMaximumSessions(1)
        concurrentSessionControlStrategy.setExceptionIfMaximumExceeded(false)

        return CompositeSessionAuthenticationStrategy(
            arrayListOf(
                concurrentSessionControlStrategy,
                RegisterSessionAuthenticationStrategy(sessionRegistry()),
                ChangeSessionIdAuthenticationStrategy()
            )
        )
    }

    @Bean
    fun sessionRegistry(): SessionRegistry {
        return SessionRegistryImpl()
    }

    @Bean
    fun authenticationManager(): AuthenticationManager {
        val provider = DaoAuthenticationProvider()
        provider.setUserDetailsService(userDetailsVerification)
        provider.setPasswordEncoder(passwordEncoder())
        return ProviderManager(provider);
    }
}


