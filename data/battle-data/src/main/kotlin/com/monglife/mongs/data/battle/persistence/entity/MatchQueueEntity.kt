package com.monglife.mongs.data.battle.persistence.entity

import com.monglife.mongs.domain.battle.model.MatchQueue

data class MatchQueueEntity(
    val deviceId: String,
    val mongId: Long,
    val matchId: Long?
) {
    /**
     * 엔티티 도메인 변환
     */
    fun toDomain(): MatchQueue {
        return MatchQueue(
            deviceId = this.deviceId,
            mongId = this.mongId,
            matchId = this.matchId,
        )
    }
}