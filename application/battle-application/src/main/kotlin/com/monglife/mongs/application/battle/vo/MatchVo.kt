package com.monglife.mongs.application.battle.vo

import com.monglife.mongs.domain.battle.enums.MatchStateCode
import com.monglife.mongs.domain.battle.model.Match

data class MatchVo(

    val matchId: Long,

    val round: Int,

    val isLastRound: Boolean,

    val stateCode: MatchStateCode,
) {
    companion object {

        /**
         * 도메인 Vo 변환
         */
        fun of(match: Match): MatchVo = MatchVo(
            matchId = match.matchId,
            round = match.round,
            isLastRound = match.isLastRound,
            stateCode = match.stateCode,
        )
    }
}
