package com.monglife.mongs.data.battle.persistence.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.monglife.mongs.domain.battle.enums.MatchRoundCode
import com.monglife.mongs.domain.battle.model.MatchPlayer

@Entity("match_player")
class MatchPlayerEntity(
    @PrimaryKey
    val playerId: String,
    val matchId: Long,
    val mongCode: String,
    val mongName: String,
    val name: String,
    val hp: Double,
    val roundCode: MatchRoundCode,
) {
    /**
     * 엔티티 도메인 변환
     */
    fun toDomain(): MatchPlayer {
        return MatchPlayer(
            playerId = this.playerId,
            matchId = this.matchId,
            mongCode = this.mongCode,
            mongName = this.mongName,
            name = this.name,
            hp = this.hp,
            roundCode = this.roundCode,
        )
    }
}