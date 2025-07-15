package com.monglife.mongs.data.battle.web.client.response

data class GetMatchOutcomeResponseDto(
    val rewardPayPoint: Int,
    val battingPayPoint: Int,
)

data class GetOverMatchResponseDto(
    val playerId: String,
    val mongCode: String,
    val mongName: String,
    val name: String,
)
