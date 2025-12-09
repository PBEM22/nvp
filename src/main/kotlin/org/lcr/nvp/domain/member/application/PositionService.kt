package org.lcr.nvp.domain.member.application

import org.lcr.nvp.domain.member.domain.Position
import org.lcr.nvp.domain.member.dto.CreatePositionRequest
import org.lcr.nvp.domain.member.dto.PositionResponse
import org.lcr.nvp.domain.member.dto.UpdatePositionRequest
import org.lcr.nvp.domain.member.repository.MemberAssignmentRepository
import org.lcr.nvp.domain.member.repository.PositionRepository
import org.lcr.nvp.global.exception.domain.DataIntegrityViolationException
import org.lcr.nvp.global.exception.domain.PositionNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class PositionService(
    private val positionRepository: PositionRepository,
    private val memberAssignmentRepository: MemberAssignmentRepository
) {

    fun getPositions(): List<PositionResponse> {
        return positionRepository.findAllByOrderByIdAsc().map { PositionResponse(it.id, it.name) }
    }

    @Transactional
    fun createPosition(request: CreatePositionRequest): PositionResponse {
        if (positionRepository.findByName(request.name) != null) {
            throw DataIntegrityViolationException("이미 존재하는 직책 이름입니다.")
        }
        val position = Position(name = request.name)
        val savedPosition = positionRepository.save(position)
        return PositionResponse(savedPosition.id, savedPosition.name)
    }

    @Transactional
    fun updatePosition(id: Long, request: UpdatePositionRequest): PositionResponse {
        val position = positionRepository.findById(id)
            .orElseThrow { PositionNotFoundException() }

        val existingPosition = positionRepository.findByName(request.name)
        if (existingPosition != null && existingPosition.id != id) {
            throw DataIntegrityViolationException("이미 존재하는 직책 이름입니다.")
        }

        position.name = request.name
        val updatedPosition = positionRepository.save(position)
        return PositionResponse(updatedPosition.id, updatedPosition.name)
    }

    @Transactional
    fun deletePosition(id: Long) {
        val position = positionRepository.findById(id)
            .orElseThrow { PositionNotFoundException() }

        if (memberAssignmentRepository.existsByPosition(position)) {
            throw DataIntegrityViolationException("해당 직책을 가진 회원이 있어 삭제할 수 없습니다.")
        }

        positionRepository.delete(position)
    }
}
