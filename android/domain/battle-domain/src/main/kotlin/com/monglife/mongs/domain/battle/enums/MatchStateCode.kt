package com.monglife.mongs.domain.battle.enums

enum class MatchStateCode(
    val message: String
) {
    /**
     * 배틀 상태
     */
    ENTERING("매치 입장중"),
    MATCH("매치 진행중"),
    END("매치 종료"),
}