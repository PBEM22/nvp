package org.lcr.nvp.domain.match.dto

import org.lcr.nvp.domain.match.domain.OpponentSchool

data class OpponentSchoolResponse(
    val id: Long,
    val schoolName: String,
    val teamName: String,
    val schoolLogoUrl: String?
) {
    companion object {
        fun from(opponentSchool: OpponentSchool): OpponentSchoolResponse {
            return OpponentSchoolResponse(
                id = opponentSchool.id!!,
                schoolName = opponentSchool.schoolName,
                teamName = opponentSchool.teamName,
                schoolLogoUrl = opponentSchool.schoolLogoUrl
            )
        }
    }
}
