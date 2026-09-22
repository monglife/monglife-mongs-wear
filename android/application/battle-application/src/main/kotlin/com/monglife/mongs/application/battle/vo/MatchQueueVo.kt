package com.monglife.mongs.application.battle.vo

import com.monglife.mongs.domain.battle.model.MatchQueue

/**
 * 매칭 큐
 */
data class MatchQueueVo(
    val deviceId: String,
    val mongId: Long,
    val matchId: Long,
    val playerId: String,
) {
    companion object {

        /**
         * 도메인 Vo 변환
         */
        fun of(matchQueue: MatchQueue) =
            MatchQueueVo(
                deviceId = matchQueue.deviceId,
                mongId = matchQueue.mongId,
                matchId = matchQueue.matchId,
                playerId = matchQueue.playerId,
            )
    }
}
