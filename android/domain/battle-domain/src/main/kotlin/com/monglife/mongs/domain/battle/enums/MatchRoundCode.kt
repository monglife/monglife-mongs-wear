package com.monglife.mongs.domain.battle.enums

enum class MatchRoundCode(
    val message: String
) {
    /**
     * 응답
     */
    NONE("원상태 유지"),
    MATCH_DEFENCE("배틀 방어"),
    MATCH_ATTACKED("배틀 피해"),
    MATCH_HEAL("배틀 회복"),
    MATCH_ATTACKED_HEAL("배틀 피해 & 회복"),
    ;
}