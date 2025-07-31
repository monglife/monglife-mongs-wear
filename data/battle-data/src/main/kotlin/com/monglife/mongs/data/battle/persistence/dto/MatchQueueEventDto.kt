package com.monglife.mongs.data.battle.persistence.dto

/**
 * 매치 큐 이벤트 Dto
 */
data class MatchQueueEventDto(
    val matchId: Long,
    val matchPlayers: List<MatchQueuePlayerEventDto>,
) {

    /**
     * 매치 플레이어 이벤트
     */
    data class MatchQueuePlayerEventDto(
        val playerId: String,
        val deviceId: String,
        val mongCode: String,
        val mongName: String,
        val name: String,
    )
}