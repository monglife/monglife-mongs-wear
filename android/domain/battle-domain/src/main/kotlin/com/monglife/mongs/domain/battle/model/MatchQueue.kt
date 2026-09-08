package com.monglife.mongs.domain.battle.model

data class MatchQueue(
    val deviceId: String,
    val mongId: Long,
    val matchId: Long,
    val playerId: String,
)