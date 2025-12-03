package org.lcr.nvp.domain.member.repository

import org.lcr.nvp.domain.member.domain.Role
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RoleRepository : JpaRepository<Role, Long> {

    /**
     * 역할 이름으로 역할을 조회합니다.
     * @param roleName 역할 이름 (e.g., "ROLE_USER")
     * @return Role 엔티티 (없으면 null)
     */
    fun findByRoleName(roleName: String): Role?
}
