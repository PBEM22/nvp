package org.lcr.nvp.domain.match.repository

import org.lcr.nvp.domain.match.domain.Match
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MatchRepository : JpaRepository<Match, Long> {
}
