package org.lcr.nvp.domain.match.domain

import jakarta.persistence.*
import org.lcr.nvp.global.common.BaseTimeEntity
import java.time.LocalDate

@Entity
@Table(name = "matches")
class Match(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "match_id")
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    val tournament: Tournament,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opponent_school_id", nullable = false)
    val opponentSchool: OpponentSchool,

    @Column(nullable = false)
    var isMale: Boolean = true,

    @Column(nullable = false)
    var isWin: Boolean = true,

    @Column(nullable = false)
    var teamScore: Int,

    @Column(nullable = false)
    var opponentScore: Int,

    var matchMvp: String? = null,

    var matchLocation: String? = null,

    var matchDate: LocalDate? = null,

    // --- MVP 및 수상자 정보 ---
    var mvpMemberId: Long? = null,
    var mvpReason: String? = null,

    var spikerMemberId: Long? = null,
    var spikerReason: String? = null,

    var defenderMemberId: Long? = null,
    var defenderReason: String? = null

) : BaseTimeEntity()
