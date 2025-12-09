package org.lcr.nvp.domain.member.repository

import org.lcr.nvp.domain.member.domain.Department
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DepartmentRepository : JpaRepository<Department, Long> {
    fun findByName(name: String): Department?
    fun findAllByOrderByIdAsc(): List<Department>
}
