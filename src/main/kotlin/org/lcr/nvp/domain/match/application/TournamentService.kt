package org.lcr.nvp.domain.match.application

import org.lcr.nvp.domain.match.domain.Tournament
import org.lcr.nvp.domain.match.dto.TournamentCreateRequest
import org.lcr.nvp.domain.match.repository.TournamentRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TournamentService(
    private val tournamentRepository: TournamentRepository
) {

    @Transactional
    fun createTournament(request: TournamentCreateRequest): Tournament {
        val tournament = Tournament(
            tournamentName = request.tournamentName,
            isSixPlayer = request.isSixPlayer
        )
        return tournamentRepository.save(tournament)
    }

    @Transactional(readOnly = true)
    fun getTournamentById(tournamentId: Long): Tournament {
        return tournamentRepository.findByIdOrNull(tournamentId)
            ?: throw NoSuchElementException("ID가 ${tournamentId}인 대회를 찾을 수 없습니다.")
    }

    @Transactional(readOnly = true)
    fun getAllTournaments(): List<Tournament> {
        return tournamentRepository.findAll()
    }

    @Transactional
    fun updateTournament(tournamentId: Long, request: TournamentCreateRequest): Tournament {
        val tournament = getTournamentById(tournamentId)
        // DTO에 필드가 2개 뿐이라 전체 업데이트로 구현.
        val updatedTournament = Tournament(
            id = tournament.id,
            tournamentName = request.tournamentName,
            isSixPlayer = request.isSixPlayer
        )
        return tournamentRepository.save(updatedTournament)
    }

    @Transactional
    fun deleteTournament(tournamentId: Long) {
        if (!tournamentRepository.existsById(tournamentId)) {
            throw NoSuchElementException("ID가 ${tournamentId}인 대회를 찾을 수 없습니다.")
        }
        tournamentRepository.deleteById(tournamentId)
    }
}
