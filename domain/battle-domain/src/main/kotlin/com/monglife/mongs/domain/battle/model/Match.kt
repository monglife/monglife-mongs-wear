package com.monglife.mongs.domain.battle.model

import com.monglife.mongs.domain.battle.enums.MatchStateCode

class Match(
    matchId: Long = -1L,
    deviceId: String,
    round: Int = 0,
    isLastRound: Boolean = false,
    stateCode: MatchStateCode = MatchStateCode.NONE,
) {
    var matchId: Long = matchId
        private set
    var deviceId: String = deviceId
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
        this.stateCode = MatchStateCode.MATCH_SEARCH
    }

    /**
     * 매치 큐 매칭
     */
    fun matching() {
        this.stateCode = MatchStateCode.MATCH_MATCHING
    }

    /**
     * 모든 매치 플레이어 입장
     */
    fun enter() {
        this.stateCode = MatchStateCode.MATCH_ENTER
    }

    /**
     * 매치 시작
     */
    fun start() {
        this.stateCode = MatchStateCode.MATCH_WAIT
    }

    /**
     * 다음 라운드 진행
     */
    fun nextRound() {
        this.stateCode = MatchStateCode.MATCH_WAIT
    }

    /**
     * 매치 선택 완료
     */
    fun pick() {
        this.stateCode = MatchStateCode.MATCH_PICK_WAIT
    }

    /**
     * 라운드 종료
     */
    fun fight() {
        this.stateCode = MatchStateCode.MATCH_FIGHT
    }

    /**
     * 매치 종료
     */
    fun over() {
        this.stateCode = MatchStateCode.MATCH_OVER
    }
}
