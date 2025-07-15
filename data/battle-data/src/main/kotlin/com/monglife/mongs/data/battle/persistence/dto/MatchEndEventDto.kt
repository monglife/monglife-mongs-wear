package com.monglife.mongs.data.battle.persistence.dto

/**
 * 매치 종료 비동기 업데이트 이벤트
 * code: MATCH_END
 */
data class MatchEndEventDto(
    val matchId: Long,
    val playerId: String,
    val name: String,
    val mongCode: String,
    val mongName: String,
)
