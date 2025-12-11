package org.lcr.nvp.domain.match.application

import org.lcr.nvp.domain.match.domain.Match
import org.lcr.nvp.domain.match.dto.MatchCreateRequest
import org.lcr.nvp.domain.match.dto.MatchResultUpdateRequest
import org.lcr.nvp.domain.match.repository.MatchRepository
import org.lcr.nvp.domain.match.repository.OpponentSchoolRepository
import org.lcr.nvp.domain.match.repository.TournamentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MatchService(
    private val matchRepository: MatchRepository,
    private val tournamentRepository: TournamentRepository,
    private val opponentSchoolRepository: OpponentSchoolRepository
) {

    @Transactional
    fun createMatch(request: MatchCreateRequest): Match {
        val tournament = tournamentRepository.findById(request.tournamentId)
            .orElseThrow { NoSuchElementException("ID가 ${request.tournamentId}인 대회를 찾을 수 없습니다.") }
        
        val opponentSchool = opponentSchoolRepository.findById(request.opponentSchoolId)
            .orElseThrow { NoSuchElementException("ID가 ${request.opponentSchoolId}인 상대 학교를 찾을 수 없습니다.") }

        val match = Match(
            tournament = tournament,
            opponentSchool = opponentSchool,
            isMale = request.isMale,
            matchLocation = request.matchLocation,
            matchDate = request.matchDate,
            teamScore = 0, // 경기는 생성 시점에는 점수가 없으므로 0으로 초기화
            opponentScore = 0,
            isWin = false // isWin 또한 경기 결과이므로 기본값으로 초기화
        )

        return matchRepository.save(match)
    }

    @Transactional
    fun updateMatchResult(matchId: Long, request: MatchResultUpdateRequest): Match {
        val match = matchRepository.findById(matchId)
            .orElseThrow { NoSuchElementException("ID가 ${matchId}인 경기를 찾을 수 없습니다.") }

        match.isWin = request.isWin
        match.teamScore = request.teamScore
        match.opponentScore = request.opponentScore

        return matchRepository.save(match)
    }
}
