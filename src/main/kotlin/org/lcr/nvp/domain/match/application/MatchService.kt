package org.lcr.nvp.domain.match.application

import org.lcr.nvp.domain.match.domain.Match
import org.lcr.nvp.domain.match.domain.MatchRecord
import org.lcr.nvp.domain.match.domain.ScoreRecord
import org.lcr.nvp.domain.match.dto.MatchCreateRequest
import org.lcr.nvp.domain.match.dto.MatchDetailResponse
import org.lcr.nvp.domain.match.dto.MatchPlayerSummaryResponse
import org.lcr.nvp.domain.match.dto.MatchResponse
import org.lcr.nvp.domain.match.dto.MatchResultUpdateRequest
import org.lcr.nvp.domain.match.dto.ScoreRecordResponse
import org.lcr.nvp.domain.match.repository.MatchRecordRepository
import org.lcr.nvp.domain.match.repository.MatchRepository
import org.lcr.nvp.domain.match.repository.OpponentSchoolRepository
import org.lcr.nvp.domain.match.repository.ScoreRecordRepository
import org.lcr.nvp.domain.match.repository.TournamentRepository
import org.lcr.nvp.domain.member.repository.MemberRepository
import org.lcr.nvp.domain.member.dto.MemberMatchResponse
import org.lcr.nvp.global.exception.BusinessException
import org.lcr.nvp.global.exception.ErrorCode
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.math.roundToInt

@Service
class MatchService(
    private val matchRepository: MatchRepository,
    private val tournamentRepository: TournamentRepository,
    private val opponentSchoolRepository: OpponentSchoolRepository,
    private val matchRecordRepository: MatchRecordRepository,
    private val memberRepository: MemberRepository,
    private val scoreRecordRepository: ScoreRecordRepository
) {

    @Transactional
    fun createMatch(request: MatchCreateRequest): Match {
        val tournament = tournamentRepository.findById(request.tournamentId)
            .orElseThrow { BusinessException(ErrorCode.TOURNAMENT_NOT_FOUND) }

        val opponentSchool = opponentSchoolRepository.findById(request.opponentSchoolId)
            .orElseThrow { NoSuchElementException("ID가 ${request.opponentSchoolId}인 상대 학교를 찾을 수 없습니다.") }

        val match = Match(
            tournament = tournament,
            opponentSchool = opponentSchool,
            isMale = request.isMale,
            matchLocation = request.matchLocation,
            matchDate = request.matchDate,
            teamScore = 0,
            opponentScore = 0,
            isWin = false
        )

        return matchRepository.save(match)
    }

    @Transactional
    fun updateMatchResult(matchId: Long, request: MatchResultUpdateRequest): MatchResponse {
        val match = matchRepository.findById(matchId)
            .orElseThrow { NoSuchElementException("ID가 ${matchId}인 경기를 찾을 수 없습니다.") }

        // 기본 경기 결과 업데이트
        match.isWin = request.isWin
        match.teamScore = request.teamScore
        match.opponentScore = request.opponentScore

        // --- 수상자 정보 업데이트 ---
        val allRecordsInMatch = matchRecordRepository.findByMatch(match)
        val recordsByMember = allRecordsInMatch.groupBy { it.member }

        // ID가 제공되지 않은 경우, 자동 선정 로직 실행
        val mvpMemberId = request.mvpMemberId ?: calculateAutoMvp(recordsByMember)
        val spikerMemberId = request.spikerMemberId ?: calculateAutoSpiker(recordsByMember)
        val defenderMemberId = request.defenderMemberId ?: calculateAutoDefender(recordsByMember)

        // MVP
        mvpMemberId?.let {
            match.mvpMemberId = it
            match.mvpReason = if (request.mvpMemberId == null) "자동 선정 (총 득점 + 디그)" else "경기 MVP"
        }

        // 공격왕
        spikerMemberId?.let {
            val records = recordsByMember.values.flatten().filter { r -> r.member.id == it }
            val totalAttackAttempt = records.sumOf(MatchRecord::attackAttempt)
            val efficiency = calculateEfficiency(
                records.sumOf(MatchRecord::attackSuccess) - records.sumOf(MatchRecord::attackError) - records.sumOf(MatchRecord::attackBlock),
                totalAttackAttempt
            )
            match.spikerMemberId = it
            match.spikerReason = if (request.spikerMemberId == null) "자동 선정 (공격 효율 ${efficiency}%)" else "공격 효율 ${efficiency}%"
        }

        // 수비왕
        defenderMemberId?.let {
            val records = recordsByMember.values.flatten().filter { r -> r.member.id == it }
            val totalDigs = records.sumOf(MatchRecord::digSuccess)
            match.defenderMemberId = it
            match.defenderReason = if (request.defenderMemberId == null) "자동 선정 (총 디그 ${totalDigs})" else "총 디그 ${totalDigs}"
        }

        val savedMatch = matchRepository.save(match)

        // --- 응답 DTO 생성을 위해 수상자 정보 조회 ---
        val awardMemberIds = listOfNotNull(savedMatch.mvpMemberId, savedMatch.spikerMemberId, savedMatch.defenderMemberId)
        val awardMembers = memberRepository.findAllById(awardMemberIds).associateBy { it.id!! }

        return MatchResponse.from(savedMatch, awardMembers)
    }

    private fun calculateAutoMvp(recordsByMember: Map<org.lcr.nvp.domain.member.domain.Member, List<MatchRecord>>): Long? {
        if (recordsByMember.isEmpty()) return null

        return recordsByMember.maxByOrNull { (_, records) ->
            val totalScore = records.sumOf(MatchRecord::score)
            val totalDigs = records.sumOf(MatchRecord::digSuccess)
            totalScore + totalDigs
        }?.key?.id
    }

    private fun calculateAutoSpiker(recordsByMember: Map<org.lcr.nvp.domain.member.domain.Member, List<MatchRecord>>): Long? {
        if (recordsByMember.isEmpty()) return null

        // 1. 팀 평균 공격 득점 계산
        val playersWithAttackAttempt = recordsByMember.filter { (_, records) -> records.sumOf(MatchRecord::attackAttempt) > 0 }
        if (playersWithAttackAttempt.isEmpty()) return null

        val totalTeamAttackSuccess = playersWithAttackAttempt.values.flatten().sumOf(MatchRecord::attackSuccess)
        val teamAverageAttackSuccess = totalTeamAttackSuccess.toDouble() / playersWithAttackAttempt.size

        // 2. 1차 후보 선정: 팀 평균 공격 득점 이상인 선수
        val primaryCandidates = playersWithAttackAttempt.filter { (_, records) ->
            records.sumOf(MatchRecord::attackSuccess) >= teamAverageAttackSuccess
        }

        // 3. 2차 후보 선정: 공격 득점 상위 3명
        val finalCandidates = primaryCandidates.entries
            .sortedByDescending { (_, records) -> records.sumOf(MatchRecord::attackSuccess) }
            .take(3)

        if (finalCandidates.isEmpty()) return null

        // 4. 최종 결정: 3명 중 공격 효율이 가장 높은 선수
        return finalCandidates.maxByOrNull { (_, records) ->
            calculateEfficiency(
                numerator = records.sumOf(MatchRecord::attackSuccess) - records.sumOf(MatchRecord::attackError) - records.sumOf(MatchRecord::attackBlock),
                denominator = records.sumOf(MatchRecord::attackAttempt)
            )
        }?.key?.id
    }

    private fun calculateAutoDefender(recordsByMember: Map<org.lcr.nvp.domain.member.domain.Member, List<MatchRecord>>): Long? {
        if (recordsByMember.isEmpty()) return null

        return recordsByMember.maxByOrNull { (_, records) ->
            records.sumOf(MatchRecord::digSuccess)
        }?.key?.id
    }

    @Transactional(readOnly = true)
    fun getMatchDetails(matchId: Long): MatchDetailResponse {
        // 1. 기본 경기 정보 조회
        val match = matchRepository.findById(matchId)
            .orElseThrow { NoSuchElementException("ID가 ${matchId}인 경기를 찾을 수 없습니다.") }

        // 2. 해당 경기의 수상자 정보 조회
        val awardMemberIds = listOfNotNull(match.mvpMemberId, match.spikerMemberId, match.defenderMemberId)
        val awardMembers = if (awardMemberIds.isNotEmpty()) {
            memberRepository.findAllById(awardMemberIds).associateBy { it.id!! }
        } else {
            emptyMap()
        }

        // 3. 해당 경기에 참여한 선수들의 경기 기록 조회
        val allRecordsInMatch = matchRecordRepository.findByMatch(match)
        val recordsByMember = allRecordsInMatch.groupBy { it.member }

        val playerMatchStats = recordsByMember.map { (member, records) ->
            MatchPlayerSummaryResponse.from(member, records)
        }.sortedWith(compareBy(nullsLast(), { it.backNumber }))

        // 4. 최종 DTO 조합
        return MatchDetailResponse.from(match, awardMembers, playerMatchStats)
    }

    @Transactional(readOnly = true)
    fun getMatchPlayerRecords(matchId: Long): List<MatchPlayerSummaryResponse> {
        val match = matchRepository.findById(matchId)
            .orElseThrow { NoSuchElementException("ID가 ${matchId}인 경기를 찾을 수 없습니다.") }

        val allRecordsInMatch = matchRecordRepository.findByMatch(match)
        val recordsByMember = allRecordsInMatch.groupBy { it.member }

        return recordsByMember.map { (member, records) ->
            MatchPlayerSummaryResponse.from(member, records)
        }.sortedWith(compareBy(nullsLast(), { it.backNumber }))
    }

    @Transactional(readOnly = true)
    fun getMatchesByMember(memberId: Long): List<MemberMatchResponse> {
        // memberId 존재 여부 확인
        if (!memberRepository.existsById(memberId)) {
            throw BusinessException(ErrorCode.MEMBER_NOT_FOUND)
        }
        val matches = matchRepository.findDistinctMatchesByMemberIdWithDetails(memberId)
        return matches.map { MemberMatchResponse.from(it) }
    }

    @Transactional(readOnly = true)
    fun getMatchesByTournament(tournamentId: Long): List<MemberMatchResponse> {
        if (!tournamentRepository.existsById(tournamentId)) {
            throw BusinessException(ErrorCode.TOURNAMENT_NOT_FOUND)
        }
        val matches = matchRepository.findMatchesByTournamentIdWithDetails(tournamentId)
        return matches.map { MemberMatchResponse.from(it) }
    }

    private fun calculateEfficiency(numerator: Int, denominator: Int): Double {
        if (denominator == 0) return 0.0
        return (numerator.toDouble() / denominator * 100).roundTo(2)
    }

    private fun Double.roundTo(decimals: Int): Double {
        val multiplier = Math.pow(10.0, decimals.toDouble())
        return (this * multiplier).roundToInt() / multiplier
    }
}
