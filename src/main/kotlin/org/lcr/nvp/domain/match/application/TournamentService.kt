package org.lcr.nvp.domain.match.application

import org.lcr.nvp.domain.match.domain.Tournament
import org.lcr.nvp.domain.match.dto.TournamentCreateRequest
import org.lcr.nvp.domain.match.repository.MatchRecordRepository
import org.lcr.nvp.domain.match.repository.MatchRepository
import org.lcr.nvp.domain.match.repository.TournamentRepository
import org.lcr.nvp.domain.member.domain.User
import org.lcr.nvp.domain.member.repository.MemberRepository
import org.lcr.nvp.domain.member.repository.UserRepository
import org.lcr.nvp.global.exception.BusinessException
import org.lcr.nvp.global.exception.ErrorCode
import org.lcr.nvp.global.exception.domain.DataIntegrityViolationException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.math.log

@Service
class TournamentService(
    private val tournamentRepository: TournamentRepository,
    private val userRepository: UserRepository,
    private val memberRepository: MemberRepository,
    private val matchRecordRepository: MatchRecordRepository,
    private val matchRepository: MatchRepository
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
        return tournamentRepository.findByIdAndDeletedAtIsNull(tournamentId)
            .orElseThrow { BusinessException(ErrorCode.TOURNAMENT_NOT_FOUND) }
    }

    @Transactional(readOnly = true)
    fun getAllTournaments(): List<Tournament> {
        return tournamentRepository.findAllActiveByOrderByIdDesc()
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
        tournament.tournamentName = request.tournamentName
        tournament.isSixPlayer = request.isSixPlayer
        return tournament // @Transactional에 의해 변경 감지(dirty checking)되어 자동 저장됨
    }

    @Transactional
    fun deleteTournament(tournamentId: Long) {
        val tournament = getTournamentById(tournamentId)

        // 해당 대회에 속한 경기가 있는지 확인
        if (matchRepository.existsByTournament(tournament)) {
            throw DataIntegrityViolationException("해당 대회에 속한 경기가 있어 삭제할 수 없습니다.")
        }

        tournament.softDelete()
    }
}
