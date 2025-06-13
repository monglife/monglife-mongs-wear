package com.monglife.mongs.domain.battle.model

import com.monglife.mongs.domain.battle.enums.MatchRoundCode

class MatchPlayer(
    playerId: String,
    mongTypeCode: String,
    hp: Double,
    roundCode: MatchRoundCode,
    deviceId: String,
) {
    var playerId: String = playerId
        private set
    var mongTypeCode: String = mongTypeCode
        private set
    var hp: Double = hp
        private set
    var roundCode: MatchRoundCode = roundCode
        private set
    var deviceId: String = deviceId
        private set}
