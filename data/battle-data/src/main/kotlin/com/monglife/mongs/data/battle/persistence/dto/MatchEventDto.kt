package com.monglife.mongs.data.battle.persistence.dto

import com.monglife.mongs.domain.battle.enums.MatchRoundCode

/**
 * 매치 이벤트
 */
data class MatchEventDto(
    val matchId: Long,
    val round: Int,
    val isLastRound: Boolean,
    val matchPlayers: List<MatchPlayerEventDto>
) {
    /**
     * 매치 플레이어 이벤트
     */
    data class MatchPlayerEventDto(
        val playerId: String,
        val deviceId: String,
        val mongId: Long,
        val mongCode: String,
        val mongName: String,
        val name: String,
        val hp: Double,
        val roundCode: MatchRoundCode,
    )
}
