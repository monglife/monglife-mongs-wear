package com.monglife.mongs.data.battle.persistence.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.monglife.mongs.domain.battle.enums.MatchStateCode
import com.monglife.mongs.domain.battle.model.Match
import java.time.LocalDateTime

@Entity("match")
data class MatchEntity(
    @PrimaryKey
    val queueId: String,
    val matchId: Long,
    val round: Int,
    val isLastRound: Boolean,
    val stateCode: MatchStateCode,
    val createdAt: LocalDateTime,
) {
    /**
     * 엔티티 도메인 변환
     */
    fun toDomain(): Match {
        return Match(
            queueId = this.queueId,
            matchId = this.matchId,
            round = this.round,
            isLastRound = this.isLastRound,
            stateCode = this.stateCode,
        )
    }
}