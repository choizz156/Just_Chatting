package com.chat.auth.application

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

data class CustomUserPrincipal(val attribute: UserAttribute) : UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority?> {
        return arrayListOf(SimpleGrantedAuthority("ROLE_${attribute.role.name}"))
    }

    override fun getPassword(): String {
        return attribute.password
    }

    override fun getUsername(): String {
        return attribute.email
    }
}