package com.monglife.mongs.domain.battle.model

import com.monglife.mongs.domain.battle.enums.MatchRoundCode

class MatchPlayer(
    playerId: String,
    deviceId: String,
    mongCode: String,
    mongName: String,
    name: String,
    hp: Double = 0.0,
    roundCode: MatchRoundCode = MatchRoundCode.NONE,
) {
    var playerId: String = playerId
        private set
    var deviceId: String = deviceId
        private set
    var mongCode: String = mongCode
        private set
    var mongName: String = mongName
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
    fun update(
        hp: Double,
        roundCode: MatchRoundCode,
    ) {
        this.hp = hp
        this.roundCode = roundCode
    }
}
