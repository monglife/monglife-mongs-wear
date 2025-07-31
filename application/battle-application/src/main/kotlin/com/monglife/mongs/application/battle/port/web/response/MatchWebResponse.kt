package com.monglife.mongs.application.battle.port.web.response

/**
 * 배틀 보상 정보 조회 응답
 */
data class GetBattleRewardResponse(
    val rewardPayPoint: Int,
    val battlePayPoint: Int,
)

/**
 * 승리 매치 플레이어 조회 응답
 */
data class GetWinnerMatchPlayerResponse(
    val playerId: String,
    val mongCode: String,
    val mongName: String,
    val name: String,
    val rewardPayPoint: Int,
)