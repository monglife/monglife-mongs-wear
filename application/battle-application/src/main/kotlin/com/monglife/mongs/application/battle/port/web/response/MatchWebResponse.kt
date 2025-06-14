package com.monglife.mongs.application.battle.port.web.response

import com.monglife.mongs.domain.battle.enums.MatchRoundCode

/**
 * 배틀 보상 정보 조회 응답
 */
data class GetBattleRewardResponse(
    val rewardPayPoint: Int,
    val battlePayPoint: Int,
)

/**
 * 매치 조회 응답
 */
data class GetMatchResponse(
    val matchId: Long,
    val round: Int,
    val isLastRound: Boolean,
    val matchPlayers: List<MatchPlayerResponse>,
) {
    /**
     * 매치 조회 -> 매치 플레이어 응답
     */
    data class MatchPlayerResponse(
        val playerId: String,
        val deviceId: String,
        val mongCode: String,
        val mongName: String,
        val name: String,
        val hp: Double,
        val roundCode: MatchRoundCode,
    )
}

/**
 * 승리 매치 플레이어 조회 응답
 */
data class GetWinnerMatchPlayerResponse(
    val playerId: String,
    val mongCode: String,
    val mongName: String,
    val name: String,
)