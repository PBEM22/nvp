package org.lcr.nvp.domain.member.repository

import jakarta.persistence.EntityManager
import org.lcr.nvp.domain.member.domain.Member
import org.lcr.nvp.domain.member.dto.MemberSearchFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

class MemberRepositoryCustomImpl(
    private val em: EntityManager
) : MemberRepositoryCustom {

    override fun findByCriteria(filter: MemberSearchFilter, pageable: Pageable): Page<Member> {
        val (whereClause, parameters) = buildWhereClause(filter)

        // 1. 데이터 조회 쿼리
        val dataQueryString = """
            SELECT DISTINCT m FROM Member m
            JOIN FETCH m.user u
            LEFT JOIN MemberAssignment ma ON ma.member = m
            LEFT JOIN ma.period p
            LEFT JOIN ma.department d
            LEFT JOIN ma.position pos
            $whereClause
        """
        val query = em.createQuery(dataQueryString, Member::class.java)
        parameters.forEach { (key, value) -> query.setParameter(key, value) }
        query.firstResult = pageable.offset.toInt()
        query.maxResults = pageable.pageSize
        val members = query.resultList

        // 2. 전체 카운트 조회 쿼리
        val countQueryString = """
            SELECT COUNT(DISTINCT m.id) FROM Member m
            LEFT JOIN MemberAssignment ma ON ma.member = m
            LEFT JOIN ma.period p
            LEFT JOIN ma.department d
            LEFT JOIN ma.position pos
            JOIN m.user u
            $whereClause
        """
        val countQuery = em.createQuery(countQueryString, Long::class.java)
        parameters.forEach { (key, value) -> countQuery.setParameter(key, value) }
        val total = countQuery.singleResult

        return PageImpl(members, pageable, total)
    }

    private fun buildWhereClause(filter: MemberSearchFilter): Pair<String, Map<String, Any>> {
        val conditions = mutableListOf<String>()
        val parameters = mutableMapOf<String, Any>()

        filter.year?.let {
            conditions.add("p.year = :year")
            parameters["year"] = it
        }
        filter.periodNumber?.let {
            conditions.add("p.periodNumber = :periodNumber")
            parameters["periodNumber"] = it
        }
        filter.departmentId?.let {
            conditions.add("d.id = :departmentId")
            parameters["departmentId"] = it
        }
        filter.positionId?.let {
            conditions.add("pos.id = :positionId")
            parameters["positionId"] = it
        }
        filter.keyword?.let {
            conditions.add("(u.name LIKE :keyword OR m.major LIKE :keyword)")
            parameters["keyword"] = "%$it%"
        }

        val whereClause = if (conditions.isNotEmpty()) "WHERE ${conditions.joinToString(" AND ")}" else ""
        return Pair(whereClause, parameters)
    }
}
