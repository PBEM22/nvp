package org.lcr.nvp.domain.match.repository

import org.lcr.nvp.domain.match.domain.Match
import org.lcr.nvp.domain.match.domain.MatchRecord
import org.lcr.nvp.domain.match.domain.Tournament
import org.lcr.nvp.domain.member.domain.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface MatchRecordRepository : JpaRepository<MatchRecord, Long> {
    /**
     * 특정 선수가 특정 대회에 출전한 기록이 있는지 확인합니다.
     * @param member 조회할 선수
     * @param tournament 조회할 대회
     * @return 출전 기록이 있으면 true, 없으면 false
     */
    fun existsByMemberAndMatch_Tournament(member: Member, tournament: Tournament): Boolean

    /**
     * 특정 경기의 특정 선수의 특정 세트 기록을 조회합니다.
     * @param match 조회할 경기
     * @param member 조회할 선수
     * @param setNumber 조회할 세트 번호
     * @return MatchRecord 엔티티 (없으면 null)
     */
    fun findByMatchAndMemberAndSetNumber(match: Match, member: Member, setNumber: Int): MatchRecord?

    /**
     * 특정 경기에 해당하는 모든 경기 기록을 조회합니다.
     * @param match 조회할 경기
     * @return 해당 경기의 모든 MatchRecord 리스트
     */
    fun findByMatch(match: Match): List<MatchRecord>

    /**
     * 특정 선수가 특정 경기에 출전한 기록이 있는지 확인합니다.
     * @param member 조회할 선수
     * @param match 조회할 경기
     * @return 출전 기록이 있으면 true, 없으면 false
     */
    fun existsByMemberAndMatch(member: Member, match: Match): Boolean

    /**
     * 특정 선수가 참여한 모든 대회를 중복 없이 조회합니다.
     * @param member 조회할 선수
     * @return 해당 선수가 참여한 Tournament 리스트
     */
    @Query("SELECT DISTINCT mr.match.tournament FROM MatchRecord mr WHERE mr.member = :member")
    fun findDistinctTournamentsByMember(@Param("member") member: Member): List<Tournament>

    /**
     * 특정 선수가 참여한 모든 경기 기록을 조회합니다.
     * @param member 조회할 선수
     * @return 해당 선수의 모든 MatchRecord 리스트
     */
    fun findByMember(member: Member): List<MatchRecord>
}
