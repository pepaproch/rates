package com.bankid.rates.rest


import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.invoke
import org.springframework.context.annotation.Configuration

import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.config.web.server.ServerHttpSecurity.http


import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            authorizeHttpRequests {
                authorize("/doc", permitAll)
                authorize(anyRequest, authenticated)
            }
            oauth2Login {
                defaultSuccessUrl("/doc" , alwaysUse = false)
            }
           // sessionManagement { sessionCreationPolicy = SessionCreationPolicy.STATELESS }
        }
        return http.build()
    }
}