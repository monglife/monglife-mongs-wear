package com.monglife.mongs.data.battle.persistence.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.monglife.mongs.domain.battle.enums.MatchRoundCode
import com.monglife.mongs.domain.battle.model.MatchPlayer

@Entity("match_player")
class MatchPlayerEntity(
    playerId: String,
    matchId: Long,
    mongCode: String,
    name: String,
    hp: Double,
    roundCode: MatchRoundCode,
) {
    @PrimaryKey
    val playerId: String = playerId

    val matchId: Long = matchId

    val mongCode: String = mongCode

    val name: String = name

    val hp: Double = hp

    val roundCode: MatchRoundCode = roundCode

    /**
     * 엔티티 도메인 변환
     */
    fun toDomain(): MatchPlayer {
        return MatchPlayer(
            playerId = this.playerId,
            matchId = this.matchId,
            mongCode = this.mongCode,
            name = this.name,
            hp = this.hp,
            roundCode = this.roundCode,
        )
    }
}