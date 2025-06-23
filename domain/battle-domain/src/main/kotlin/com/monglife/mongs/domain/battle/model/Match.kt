package com.monglife.mongs.domain.battle.model

import com.monglife.mongs.domain.battle.enums.MatchStateCode
import java.time.LocalDateTime
import java.util.UUID

class Match(
    val queueId: String = UUID.randomUUID().toString(),
    matchId: Long = -1L,
    round: Int = 0,
    isLastRound: Boolean = false,
    stateCode: MatchStateCode = MatchStateCode.NONE,
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

    /**
     * 매치 큐
     */
    fun search() {
        this.stateCode = MatchStateCode.SEARCH
    }

    /**
     * 매치 큐 매칭
     */
    fun matching(matchId: Long) {
        this.matchId = matchId
        this.stateCode = MatchStateCode.MATCHING
    }

    /**
     * 매치 시작
     */
    fun start() {
        this.stateCode = MatchStateCode.MATCH
    }

    /**
     * 라운드 종료
     */
    fun update(
        round: Int,
        isLastRound: Boolean,
    ) {
        this.round = round
        this.isLastRound = isLastRound
    }

    /**
     * 매치 종료
     */
    fun end() {
        this.isLastRound = true
        this.stateCode = MatchStateCode.NONE
    }
}
