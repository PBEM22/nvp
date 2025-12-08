package org.lcr.nvp.domain.member.application

import org.lcr.nvp.domain.member.domain.RoleMapping
import org.lcr.nvp.domain.member.dto.CreateRoleMappingRequest
import org.lcr.nvp.domain.member.dto.RoleMappingResponse
import org.lcr.nvp.domain.member.dto.UpdateRoleMappingRequest
import org.lcr.nvp.domain.member.repository.DepartmentRepository
import org.lcr.nvp.domain.member.repository.PositionRepository
import org.lcr.nvp.domain.member.repository.RoleMappingRepository
import org.lcr.nvp.global.exception.domain.DataIntegrityViolationException
import org.lcr.nvp.global.exception.domain.DepartmentNotFoundException
import org.lcr.nvp.global.exception.domain.PositionNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class RoleMappingService(
    private val roleMappingRepository: RoleMappingRepository,
    private val departmentRepository: DepartmentRepository,
    private val positionRepository: PositionRepository
) {

    fun getAllMappings(): List<RoleMappingResponse> {
        return roleMappingRepository.findAll().map { it.toResponse() }
    }

    @Transactional
    fun createMapping(request: CreateRoleMappingRequest): RoleMappingResponse {
        val department = departmentRepository.findById(request.departmentId)
            .orElseThrow { DepartmentNotFoundException() }
        val position = positionRepository.findById(request.positionId)
            .orElseThrow { PositionNotFoundException() }

        if (roleMappingRepository.findByDepartmentAndPosition(department, position) != null) {
            throw DataIntegrityViolationException("이미 해당 부서와 직책에 대한 매핑 규칙이 존재합니다.")
        }

        val roleMapping = RoleMapping(
            department = department,
            position = position,
            displayName = request.displayName
        )
        val savedMapping = roleMappingRepository.save(roleMapping)
        return savedMapping.toResponse()
    }

    @Transactional
    fun updateMapping(id: Long, request: UpdateRoleMappingRequest): RoleMappingResponse {
        val roleMapping = roleMappingRepository.findById(id)
            .orElseThrow { DataIntegrityViolationException("해당 매핑을 찾을 수 없습니다.") } // Or create a new exception

        roleMapping.displayName = request.displayName
        val updatedMapping = roleMappingRepository.save(roleMapping)
        return updatedMapping.toResponse()
    }

    @Transactional
    fun deleteMapping(id: Long) {
        if (!roleMappingRepository.existsById(id)) {
            throw DataIntegrityViolationException("해당 매핑을 찾을 수 없습니다.")
        }
        roleMappingRepository.deleteById(id)
    }

    private fun RoleMapping.toResponse(): RoleMappingResponse = RoleMappingResponse(
        id = this.id,
        departmentId = this.department.id,
        departmentName = this.department.name,
        positionId = this.position.id,
        positionName = this.position.name,
        displayName = this.displayName
    )
}
