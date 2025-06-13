package com.monglife.mongs.application.battle.vo

import com.monglife.mongs.domain.battle.enums.MatchRoundCode
import com.monglife.mongs.domain.battle.model.MatchPlayer

data class MatchPlayerVo(

    val playerId: String,

    val mongCode: String,

    val hp: Double,

    val roundCode: MatchRoundCode,

    val deviceId: String,
) {
    companion object {

        /**
         * 도메인 Vo 변환
         */
        fun of(matchPlayer: MatchPlayer): MatchPlayerVo = MatchPlayerVo(
            playerId = matchPlayer.playerId,
            mongCode = matchPlayer.mongTypeCode,
            hp = matchPlayer.hp,
            roundCode = matchPlayer.roundCode,
            deviceId = matchPlayer.deviceId,
        )
    }
}
