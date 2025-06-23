package com.monglife.mongs.domain.battle.enums

enum class MatchStateCode(
    val message: String
) {
    /**
     * 배틀 상태
     */
    NONE("없음"),                     // 매치 종료
    SEARCH("배틀 검색중"),           // 매칭 진행중
    MATCHING("배틀 매칭 성공"),      // 입장 대기
    MATCH("매치 진행중"),            // 매치 화면
}