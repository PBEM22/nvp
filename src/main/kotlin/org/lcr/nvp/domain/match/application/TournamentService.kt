package org.lcr.nvp.domain.match.application

import org.lcr.nvp.domain.match.domain.Tournament
import org.lcr.nvp.domain.match.dto.TournamentCreateRequest
import org.lcr.nvp.domain.match.repository.MatchRecordRepository
import org.lcr.nvp.domain.match.repository.TournamentRepository
import org.lcr.nvp.domain.member.domain.User
import org.lcr.nvp.domain.member.repository.MemberRepository
import org.lcr.nvp.domain.member.repository.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.math.log

@Service
class TournamentService(
    private val tournamentRepository: TournamentRepository,
    private val userRepository: UserRepository,
    private val memberRepository: MemberRepository,
    private val matchRecordRepository: MatchRecordRepository
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
        return tournamentRepository.findAllByOrderByIdDesc()
    }

    @Transactional(readOnly = true)
    fun getMyParticipatedTournaments(identifier: String): List<Tournament> {

        val user = if (identifier.contains("@")) {
            println("이메일")
            userRepository.findByEmail(identifier)
        } else {
            println("인증번호")
            userRepository.findByProviderId(identifier)
        } ?: throw NoSuchElementException("ID 또는 이메일이 ${identifier}인 사용자를 찾을 수 없습니다.")

        val member = memberRepository.findByUser(user)
            ?: throw NoSuchElementException("해당 사용자는 회원이 아닙니다.")

        return matchRecordRepository.findDistinctTournamentsByMember(member)
            .sortedBy { it.tournamentName }
    }

    @Transactional(readOnly = true)
    fun getTournamentsByMemberId(memberId: Long): List<Tournament> {
        val member = memberRepository.findById(memberId)
            .orElseThrow { NoSuchElementException("ID가 ${memberId}인 회원을 찾을 수 없습니다.") }

        return matchRecordRepository.findDistinctTournamentsByMember(member)
            .sortedBy { it.tournamentName }
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
