package com.monglife.mongs.domain.battle.enums

enum class MatchStateCode(
    val message: String
) {
    /**
     * 배틀 상태
     */
    NONE("없음"),
    MATCH_SEARCH("배틀 검색중"),                         // USE CASE (DEFAULT)
    MATCH_MATCHING("배틀 매칭 성공"),                    // MQTT
    MATCH_ENTER("배틀 입장 완료"),                       // MQTT
    MATCH_WAIT("배틀 라운드 선택 전 대기중"),            // MQTT & USE CASE
    MATCH_PICK_WAIT("배틀 라운드 선택 후 대기중"),       // USE CASE
    MATCH_FIGHT("배틀 라운드 종료"),                     // MQTT
    MATCH_OVER("배틀 종료"),                             // MQTT & USE CASE
}