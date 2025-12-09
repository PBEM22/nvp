package org.lcr.nvp.domain.match.dto

data class PlayerStatDto(
    val setNumber: Int,
    val backNumber: Int,
    val playerName: String,
    val score: Int,
    val attackAttempt: Int,
    val attackSuccess: Int,
    val attackError: Int,
    val attackBlock: Int,
    val receiveAttempt: Int,
    val receivePerfect: Int,
    val receiveError: Int,
    val blockAttempt: Int,
    val blockSuccess: Int,
    val blockEffective: Int,
    val blockError: Int,
    val blockFault: Int,
    val serveAttempt: Int,
    val serveAce: Int,
    val serveError: Int,
    val digAttempt: Int,
    val digSuccess: Int,
    val digError: Int,
    val tossAttempt: Int,
    val tossSuccess: Int,
    val tossError: Int
)
