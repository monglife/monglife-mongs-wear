package com.monglife.mongs.domain.battle.model

import com.monglife.mongs.domain.battle.enums.MatchStateCode
import java.time.LocalDateTime

class Match(
    matchId: Long,
    round: Int,
    isLastRound: Boolean,
    stateCode: MatchStateCode,
    matchPlayers: List<MatchPlayer>,
    val createdAt: LocalDateTime = LocalDateTime.now(),
) {
    var matchId: Long = matchId
        private set
    var round: Int = round
        private set
    var isLastRound: Boolean = isLastRound
        private set
    var stateCode: MatchStateCode = stateCode
        private set
    var matchPlayers: List<MatchPlayer> = matchPlayers
        private set
}
