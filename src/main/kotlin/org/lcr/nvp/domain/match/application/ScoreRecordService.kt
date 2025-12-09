package org.lcr.nvp.domain.match.application

import org.lcr.nvp.domain.match.domain.ScoreRecord
import org.lcr.nvp.domain.match.repository.ScoreRecordRepository
import org.lcr.nvp.domain.member.repository.MemberRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ScoreRecordService(
    private val scoreRecordRepository: ScoreRecordRepository,
    private val memberRepository: MemberRepository
) {

    @Transactional(readOnly = true)
    fun getScoreRecordByMember(memberId: Long): ScoreRecord {
        val member = memberRepository.findByIdOrNull(memberId)
            ?: throw NoSuchElementException("ID가 ${memberId}인 회원을 찾을 수 없습니다.")

        // 기록이 없는 선수일 경우, 모든 값이 0인 비어있는 ScoreRecord를 반환
        return scoreRecordRepository.findByMember(member)
            ?: ScoreRecord(member = member)
    }
}
