package org.lcr.nvp.domain.member.repository

import org.lcr.nvp.domain.member.domain.Member
import org.lcr.nvp.domain.member.dto.MemberSearchFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface MemberRepositoryCustom {
    fun findByCriteria(filter: MemberSearchFilter, pageable: Pageable): Page<Member>
}
