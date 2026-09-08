package com.monglife.mongs.application.battle.vo

import com.monglife.mongs.domain.battle.enums.MatchRoundCode
import com.monglife.mongs.domain.battle.enums.MatchStateCode
import com.monglife.mongs.domain.battle.model.Match
import com.monglife.mongs.domain.battle.model.MatchPlayer
import java.time.LocalDateTime

data class MatchVo(
    val matchId: Long,
    val round: Int,
    val isLastRound: Boolean,
    val stateCode: MatchStateCode,
    val createdAt: LocalDateTime,
    val matchPlayers: List<MatchPlayerVo>,
) {
    companion object {
        fun of(match: Match, deviceId: String) = MatchVo(
            matchId = match.matchId,
            round = match.round,
            isLastRound = match.isLastRound,
            stateCode = match.stateCode,
            createdAt = match.createdAt,
            matchPlayers = match.matchPlayers.map { MatchPlayerVo.of(it, deviceId) }
        )
    }

    data class MatchPlayerVo(
        val playerId: String,
        val deviceId: String,
        val mongCode: String,
        val mongName: String,
        val name: String,
        val hp: Float,
        val roundCode: MatchRoundCode,
        val isMe: Boolean,
    ) {
        companion object {
            fun of(matchPlayer: MatchPlayer, deviceId: String) = MatchPlayerVo(
                playerId = matchPlayer.playerId,
                deviceId = matchPlayer.deviceId,
                mongCode = matchPlayer.mongCode,
                mongName = matchPlayer.mongName,
                name = matchPlayer.name,
                hp = matchPlayer.hp,
                roundCode = matchPlayer.roundCode,
                isMe = matchPlayer.deviceId == deviceId,
            )
        }
    }
}
