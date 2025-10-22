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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CustomUserPrincipal

        return attribute.email == other.username
    }

    override fun hashCode(): Int {
        return attribute.email.hashCode()
    }
}