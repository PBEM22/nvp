package org.lcr.nvp.domain.member.repository

import org.lcr.nvp.domain.member.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?
    fun findByProviderId(providerId: String): User?
}

