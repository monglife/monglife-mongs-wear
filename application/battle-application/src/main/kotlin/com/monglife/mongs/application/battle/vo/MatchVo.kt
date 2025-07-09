package com.monglife.mongs.application.battle.vo

import com.monglife.mongs.domain.battle.enums.MatchRoundCode
import com.monglife.mongs.domain.battle.enums.MatchStateCode
import com.monglife.mongs.domain.battle.model.Match
import com.monglife.mongs.domain.battle.model.MatchPlayer

data class MatchVo(
    val matchId: Long,
    val round: Int,
    val isLastRound: Boolean,
    val stateCode: MatchStateCode,
    val matchPlayers: List<MatchPlayerVo>,
) {
    companion object {

        /**
         * 도메인 Vo 변환
         */
        fun of(match: Match, matchPlayers: List<MatchPlayer>): MatchVo = MatchVo(
            matchId = match.matchId,
            round = match.round,
            isLastRound = match.isLastRound,
            stateCode = match.stateCode,
            matchPlayers = matchPlayers.map { MatchPlayerVo.of(it) }
        )
    }

    data class MatchPlayerVo(
        val playerId: String,
        val mongCode: String,
        val mongName: String,
        val name: String,
        val hp: Double,
        val roundCode: MatchRoundCode,
    ) {
        companion object {

            /**
             * 도메인 Vo 변환
             */
            fun of(matchPlayer: MatchPlayer): MatchPlayerVo = MatchPlayerVo(
                playerId = matchPlayer.playerId,
                mongCode = matchPlayer.mongCode,
                mongName = matchPlayer.mongName,
                name = matchPlayer.name,
                hp = matchPlayer.hp,
                roundCode = matchPlayer.roundCode,
            )
        }
    }
}
