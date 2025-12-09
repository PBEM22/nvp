package org.lcr.nvp.domain.match.domain

import jakarta.persistence.*
import org.lcr.nvp.domain.member.domain.Member

@Entity
@Table(name = "match_records")
class MatchRecord(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "match_record_id")
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    val match: Match,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    @Column(nullable = false)
    var setNumber: Int,

    @Column(nullable = false)
    var score: Int = 0,

    // 공격
    var attackAttempt: Int = 0,
    var attackSuccess: Int = 0,
    var attackBlock: Int = 0,
    var attackError: Int = 0,
    var attackSuccessRate: Double = 0.0,
    var attackEfficiency: Double = 0.0,
    var attackPossession: Double = 0.0,

    // 리시브
    var receiveAttempt: Int = 0,
    var receivePerfect: Int = 0,
    var receiveError: Int = 0,
    var receiveSuccessRate: Double = 0.0,
    var receiveEfficiency: Double = 0.0,
    var receivePossession: Double = 0.0,

    // 블로킹
    var blockAttempt: Int = 0,
    var blockSuccess: Int = 0,
    var blockEffective: Int = 0,
    var blockError: Int = 0,
    var blockFault: Int = 0,
    var blockSuccessRate: Double = 0.0,
    var blockEfficiency: Double = 0.0,
    var blockPossession: Double = 0.0,
    var blockAssist: Int = 0,

    // 서브
    var serveAttempt: Int = 0,
    var serveAce: Int = 0,
    var serveError: Int = 0,
    var serveSuccessRate: Double = 0.0,
    var serveEfficiency: Double = 0.0,
    var servePossession: Double = 0.0,

    // 디그
    var digAttempt: Int = 0,
    var digSuccess: Int = 0,
    var digError: Int = 0,
    var digSuccessRate: Double = 0.0,
    var digEfficiency: Double = 0.0,
    var digPossession: Double = 0.0,

    // 토스
    var tossAttempt: Int = 0,
    var tossSuccess: Int = 0,
    var tossError: Int = 0,
    var tossSuccessRate: Double = 0.0,
    var tossEfficiency: Double = 0.0,
    var tossPossession: Double = 0.0
)
