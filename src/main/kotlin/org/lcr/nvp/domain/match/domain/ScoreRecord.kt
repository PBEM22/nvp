package org.lcr.nvp.domain.match.domain

import jakarta.persistence.*
import org.lcr.nvp.domain.member.domain.Member

@Entity
@Table(name = "score_records")
class ScoreRecord(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "score_record_id")
    val id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    /** 출전한 총 대회 수 */
    var tournamentsPlayed: Int = 0,
    /** 출전한 총 경기 수 */
    var matchesPlayed: Int = 0,
    /** 출전한 총 세트 수 */
    var setsPlayed: Int = 0,
    /** 누적 총 득점 */
    var totalScore: Int = 0,

    // --- 누적 공격 기록 ---
    var totalAttackAttempt: Int = 0,
    var totalAttackSuccess: Int = 0,
    var totalAttackError: Int = 0,
    var totalAttackBlock: Int = 0,

    // --- 누적 리시브 기록 ---
    var totalReceiveAttempt: Int = 0,
    var totalReceivePerfect: Int = 0,
    var totalReceiveError: Int = 0,

    // --- 누적 블로킹 기록 ---
    var totalBlockAttempt: Int = 0,
    var totalBlockSuccess: Int = 0,
    var totalBlockEffective: Int = 0,
    var totalBlockError: Int = 0,
    var totalBlockFault: Int = 0,
    var totalBlockAssist: Int = 0,

    // --- 누적 서브 기록 ---
    var totalServeAttempt: Int = 0,
    var totalServeAce: Int = 0,
    var totalServeError: Int = 0,

    // --- 누적 디그 기록 ---
    var totalDigAttempt: Int = 0,
    var totalDigSuccess: Int = 0,
    var totalDigError: Int = 0,

    // --- 누적 세트 기록 ---
    var totalTossAttempt: Int = 0,
    var totalTossSuccess: Int = 0,
    var totalTossError: Int = 0
) {
    /**
     * 새로운 세트 기록을 누적 스탯에 더합니다.
     */
    fun addMatchRecord(record: MatchRecord) {
        this.setsPlayed += 1
        this.totalScore += record.score

        this.totalAttackAttempt += record.attackAttempt
        this.totalAttackSuccess += record.attackSuccess
        this.totalAttackError += record.attackError
        this.totalAttackBlock += record.attackBlock

        this.totalReceiveAttempt += record.receiveAttempt
        this.totalReceivePerfect += record.receivePerfect
        this.totalReceiveError += record.receiveError

        this.totalBlockAttempt += record.blockAttempt
        this.totalBlockSuccess += record.blockSuccess
        this.totalBlockEffective += record.blockEffective
        this.totalBlockError += record.blockError
        this.totalBlockFault += record.blockFault
        this.totalBlockAssist += record.blockAssist

        this.totalServeAttempt += record.serveAttempt
        this.totalServeAce += record.serveAce
        this.totalServeError += record.serveError

        this.totalDigAttempt += record.digAttempt
        this.totalDigSuccess += record.digSuccess
        this.totalDigError += record.digError

        this.totalTossAttempt += record.tossAttempt
        this.totalTossSuccess += record.tossSuccess
        this.totalTossError += record.tossError
    }

    /**
     * 기존 세트 기록을 누적 스탯에서 뺍니다. (업데이트 시 사용)
     */
    fun subtractMatchRecord(record: MatchRecord) {
        this.setsPlayed -= 1
        this.totalScore -= record.score

        this.totalAttackAttempt -= record.attackAttempt
        this.totalAttackSuccess -= record.attackSuccess
        this.totalAttackError -= record.attackError
        this.totalAttackBlock -= record.attackBlock

        this.totalReceiveAttempt -= record.receiveAttempt
        this.totalReceivePerfect -= record.receivePerfect
        this.totalReceiveError -= record.receiveError

        this.totalBlockAttempt -= record.blockAttempt
        this.totalBlockSuccess -= record.blockSuccess
        this.totalBlockEffective -= record.blockEffective
        this.totalBlockError -= record.blockError
        this.totalBlockFault -= record.blockFault
        this.totalBlockAssist -= record.blockAssist

        this.totalServeAttempt -= record.serveAttempt
        this.totalServeAce -= record.serveAce
        this.totalServeError -= record.serveError

        this.totalDigAttempt -= record.digAttempt
        this.totalDigSuccess -= record.digSuccess
        this.totalDigError -= record.digError

        this.totalTossAttempt -= record.tossAttempt
        this.totalTossSuccess -= record.tossSuccess
        this.totalTossError -= record.tossError
    }
}
