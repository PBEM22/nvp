package org.lcr.nvp.domain.member.application

import org.lcr.nvp.domain.member.domain.Department
import org.lcr.nvp.domain.member.dto.CreateDepartmentRequest
import org.lcr.nvp.domain.member.dto.DepartmentResponse
import org.lcr.nvp.domain.member.dto.UpdateDepartmentRequest
import org.lcr.nvp.domain.member.repository.DepartmentRepository
import org.lcr.nvp.domain.member.repository.MemberAssignmentRepository
import org.lcr.nvp.global.exception.domain.DataIntegrityViolationException
import org.lcr.nvp.global.exception.domain.DepartmentNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DepartmentService(
    private val departmentRepository: DepartmentRepository,
    private val memberAssignmentRepository: MemberAssignmentRepository
) {

    fun getDepartments(): List<DepartmentResponse> {
        return departmentRepository.findAllByOrderByIdAsc().map { DepartmentResponse(it.id, it.name) }
    }

    @Transactional
    fun createDepartment(request: CreateDepartmentRequest): DepartmentResponse {
        if (departmentRepository.findByName(request.name) != null) {
            throw DataIntegrityViolationException("이미 존재하는 부서 이름입니다.")
        }
        val department = Department(name = request.name)
        val savedDepartment = departmentRepository.save(department)
        return DepartmentResponse(savedDepartment.id, savedDepartment.name)
    }

    @Transactional
    fun updateDepartment(id: Long, request: UpdateDepartmentRequest): DepartmentResponse {
        val department = departmentRepository.findById(id)
            .orElseThrow { DepartmentNotFoundException() }

        val existingDepartment = departmentRepository.findByName(request.name)
        if (existingDepartment != null && existingDepartment.id != id) {
            throw DataIntegrityViolationException("이미 존재하는 부서 이름입니다.")
        }

        department.name = request.name
        val updatedDepartment = departmentRepository.save(department)
        return DepartmentResponse(updatedDepartment.id, updatedDepartment.name)
    }

    @Transactional
    fun deleteDepartment(id: Long) {
        val department = departmentRepository.findById(id)
            .orElseThrow { DepartmentNotFoundException() }

        if (memberAssignmentRepository.existsByDepartment(department)) {
            throw DataIntegrityViolationException("해당 부서에 소속된 회원이 있어 삭제할 수 없습니다.")
        }

        departmentRepository.delete(department)
    }
}
