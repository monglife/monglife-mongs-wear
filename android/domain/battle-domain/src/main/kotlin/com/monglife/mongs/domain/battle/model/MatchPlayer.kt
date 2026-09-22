package com.monglife.mongs.domain.battle.model

import com.monglife.mongs.domain.battle.enums.MatchRoundCode

class MatchPlayer(
    playerId: String,
    deviceId: String,
    mongCode: String,
    mongName: String,
    name: String,
    hp: Float,
    roundCode: MatchRoundCode,
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
    var hp: Float = hp
        private set
    var roundCode: MatchRoundCode = roundCode
        private set
}
