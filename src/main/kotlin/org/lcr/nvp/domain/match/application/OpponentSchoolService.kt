package org.lcr.nvp.domain.match.application

import org.lcr.nvp.domain.match.domain.OpponentSchool
import org.lcr.nvp.domain.match.dto.OpponentSchoolCreateRequest
import org.lcr.nvp.domain.match.repository.OpponentSchoolRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OpponentSchoolService(
    private val opponentSchoolRepository: OpponentSchoolRepository
) {

    @Transactional
    fun createOpponentSchool(request: OpponentSchoolCreateRequest): OpponentSchool {
        val opponentSchool = OpponentSchool(
            schoolName = request.schoolName,
            teamName = request.teamName,
            schoolLogoUrl = request.schoolLogoUrl
        )
        return opponentSchoolRepository.save(opponentSchool)
    }

    @Transactional(readOnly = true)
    fun getOpponentSchoolById(schoolId: Long): OpponentSchool {
        return opponentSchoolRepository.findByIdOrNull(schoolId)
            ?: throw NoSuchElementException("ID가 ${schoolId}인 상대 학교를 찾을 수 없습니다.")
    }

    @Transactional(readOnly = true)
    fun getAllOpponentSchools(): List<OpponentSchool> {
        return opponentSchoolRepository.findAllByOrderBySchoolNameAsc()
    }

    @Transactional
    fun updateOpponentSchool(schoolId: Long, request: OpponentSchoolCreateRequest): OpponentSchool {
        val school = getOpponentSchoolById(schoolId)
        val updatedSchool = OpponentSchool(
            id = school.id,
            schoolName = request.schoolName,
            teamName = request.teamName,
            schoolLogoUrl = request.schoolLogoUrl
        )
        return opponentSchoolRepository.save(updatedSchool)
    }

    @Transactional
    fun deleteOpponentSchool(schoolId: Long) {
        if (!opponentSchoolRepository.existsById(schoolId)) {
            throw NoSuchElementException("ID가 ${schoolId}인 상대 학교를 찾을 수 없습니다.")
        }
        opponentSchoolRepository.deleteById(schoolId)
    }
}
