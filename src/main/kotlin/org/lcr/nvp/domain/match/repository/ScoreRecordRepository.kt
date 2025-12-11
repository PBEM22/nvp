package org.lcr.nvp.domain.match.repository

import org.lcr.nvp.domain.match.domain.ScoreRecord
import org.lcr.nvp.domain.member.domain.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ScoreRecordRepository : JpaRepository<ScoreRecord, Long> {
    fun findByMember(member: Member): ScoreRecord?
}
