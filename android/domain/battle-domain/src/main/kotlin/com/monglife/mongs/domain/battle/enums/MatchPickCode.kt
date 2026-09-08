package com.monglife.mongs.domain.battle.enums

enum class MatchPickCode(
    val message: String
) {
    /**
     * 선택
     */
    MATCH_PICK_ATTACK("배틀 공격 선택"),
    MATCH_PICK_DEFENCE("배틀 방어 선택"),
    MATCH_PICK_HEAL("배틀 회복 선택"),
    ;
}