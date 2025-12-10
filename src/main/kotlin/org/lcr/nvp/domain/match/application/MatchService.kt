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
            .orElseThrow { NoSuchElementException("ID가 ${request.tournamentId}인 대회를 찾을 수 없습니다.") }
        
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
        val recordsByMemberId = allRecordsInMatch.groupBy { it.member.id!! }

        // MVP
        request.mvpMemberId?.let {
            match.mvpMemberId = it
            match.mvpReason = "경기 MVP"
        }

        // 공격왕
        request.spikerMemberId?.let {
            val records = recordsByMemberId[it] ?: emptyList()
            val totalAttackAttempt = records.sumOf { r: MatchRecord -> r.attackAttempt }
            val efficiency = calculateEfficiency(
                records.sumOf { r: MatchRecord -> r.attackSuccess } - records.sumOf { r: MatchRecord -> r.attackError } - records.sumOf { r: MatchRecord -> r.attackBlock },
                totalAttackAttempt
            )
            match.spikerMemberId = it
            match.spikerReason = "공격 효율 ${efficiency}%"
        }

        // 수비왕
        request.defenderMemberId?.let {
            val records = recordsByMemberId[it] ?: emptyList()
            val totalBlocks = records.sumOf { r: MatchRecord -> r.blockSuccess }
            val totalDigs = records.sumOf { r: MatchRecord -> r.digSuccess }
            match.defenderMemberId = it
            match.defenderReason = "블로킹 ${totalBlocks} + 디그 ${totalDigs}"
        }

        val savedMatch = matchRepository.save(match)

        // --- 응답 DTO 생성을 위해 수상자 정보 조회 ---
        val awardMemberIds = listOfNotNull(savedMatch.mvpMemberId, savedMatch.spikerMemberId, savedMatch.defenderMemberId)
        val awardMembers = memberRepository.findAllById(awardMemberIds).associateBy { it.id!! }

        return MatchResponse.from(savedMatch, awardMembers)
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

        // 3. 해당 경기에 참여한 선수들의 통산 기록 조회
        val allRecordsInMatch = matchRecordRepository.findByMatch(match)
        val participatingMembers = allRecordsInMatch.map { it.member }.distinct()
        
        val scoreRecords = if (participatingMembers.isNotEmpty()) {
            scoreRecordRepository.findByMemberIn(participatingMembers)
        } else {
            emptyList()
        }
        
        // ScoreRecord가 없는 선수들을 위해 비어있는 ScoreRecord 생성
        val existingScoreRecordMembers = scoreRecords.map { it.member }
        val missingScoreRecords = participatingMembers.filterNot { existingScoreRecordMembers.contains(it) }
            .map { ScoreRecord(member = it) }

        val finalScoreRecords = (scoreRecords + missingScoreRecords)
            .map { ScoreRecordResponse.from(it) }

        // 4. 최종 DTO 조합
        return MatchDetailResponse.from(match, awardMembers, finalScoreRecords)
    }

    @Transactional(readOnly = true)
    fun getMatchPlayerRecords(matchId: Long): List<MatchPlayerSummaryResponse> {
        val match = matchRepository.findById(matchId)
            .orElseThrow { NoSuchElementException("ID가 ${matchId}인 경기를 찾을 수 없습니다.") }

        val allRecordsInMatch = matchRecordRepository.findByMatch(match)
        val recordsByMember = allRecordsInMatch.groupBy { it.member }

        return recordsByMember.map { (member, records) ->
            MatchPlayerSummaryResponse.from(member, records)
        }
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
