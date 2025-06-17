package com.monglife.mongs.data.battle.persistence.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.monglife.mongs.domain.battle.enums.MatchStateCode
import com.monglife.mongs.domain.battle.model.Match
import java.time.LocalDateTime

@Entity("match")
class MatchEntity(
    queueId: String,
    matchId: Long,
    round: Int,
    isLastRound: Boolean,
    stateCode: MatchStateCode,
) {
    @PrimaryKey
    val queueId: String = queueId

    val matchId: Long = matchId

    val round: Int = round

    val isLastRound: Boolean = isLastRound

    val stateCode: MatchStateCode = stateCode

    val createdAt: LocalDateTime = LocalDateTime.now()

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