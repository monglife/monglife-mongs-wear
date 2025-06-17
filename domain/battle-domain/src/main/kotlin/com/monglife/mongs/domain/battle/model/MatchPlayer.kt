package com.monglife.mongs.domain.battle.model

import com.monglife.mongs.domain.battle.enums.MatchRoundCode

class MatchPlayer(
    playerId: String,
    matchId: Long,
    mongCode: String,
    name: String,
    hp: Double,
    roundCode: MatchRoundCode,
) {
    var playerId: String = playerId
        private set
    var matchId: Long = matchId
        private set
    var mongCode: String = mongCode
        private set
    var name: String = name
        private set
    var hp: Double = hp
        private set
    var roundCode: MatchRoundCode = roundCode
        private set

    /**
     * 업데이트
     */
    fun fight(
        hp: Double,
        roundCode: MatchRoundCode,
    ) {
        this.hp = hp
        this.roundCode = roundCode
    }
}
