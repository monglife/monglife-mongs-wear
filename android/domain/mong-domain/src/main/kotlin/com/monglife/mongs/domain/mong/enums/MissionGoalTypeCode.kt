package com.monglife.mongs.domain.mong.enums

/**
 * 미션 목표 타입.
 *
 * 서버가 일간/주간/월간이 겹치지 않게 쓰는 축이다. 같은 액션이라도 타입이 다르면 다른 미션이다 -
 * 일간 "밥 5번 주기"(COUNT)와 주간 "서로 다른 음식 5종 먹이기"(DISTINCT)는 서로 다른 미션이다.
 */
enum class MissionGoalTypeCode(
    val description: String,
) {
    COUNT("실행 횟수"),
    DISTINCT("서로 다른 대상 수"),
    ACCUMULATE("수치 누적"),
    ;
}
