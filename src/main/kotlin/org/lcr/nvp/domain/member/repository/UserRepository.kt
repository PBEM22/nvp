package org.lcr.nvp.domain.member.repository

import org.lcr.nvp.domain.member.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {

    /**
     * 이메일로 사용자를 조회합니다.
     * @param email 사용자 이메일
     * @return User 엔티티 (없으면 null)
     */
    fun findByEmail(email: String): User?
}
