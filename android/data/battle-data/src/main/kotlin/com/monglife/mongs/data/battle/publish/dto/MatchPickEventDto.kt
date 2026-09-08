package com.monglife.mongs.data.battle.publish.dto

data class MatchPickEventDto(
    val playerId: String,
    val targetPlayerId: String,
    val pickCode: String,
)