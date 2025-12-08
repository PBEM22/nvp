package org.lcr.nvp.domain.member.repository

import org.lcr.nvp.domain.member.domain.Department
import org.lcr.nvp.domain.member.domain.Position
import org.lcr.nvp.domain.member.domain.RoleMapping
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface RoleMappingRepository : JpaRepository<RoleMapping, Long> {

    fun findByDepartmentAndPosition(department: Department, position: Position): RoleMapping?

    @Query("SELECT rm FROM RoleMapping rm JOIN FETCH rm.department JOIN FETCH rm.position ORDER BY rm.department.name, rm.position.name")
    override fun findAll(): List<RoleMapping>
}
