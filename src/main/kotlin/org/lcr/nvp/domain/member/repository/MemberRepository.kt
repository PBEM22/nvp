package org.lcr.nvp.domain.member.repository

import org.lcr.nvp.domain.member.domain.Member
import org.lcr.nvp.domain.member.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MemberRepository : JpaRepository<Member, Long> {
    /**
     * User 엔티티로 Member 엔티티를 조회합니다.
     * @param user User 엔티티
     * @return Member 엔티티 (없으면 null)
     */
    fun findByUser(user: User): Member?
}
