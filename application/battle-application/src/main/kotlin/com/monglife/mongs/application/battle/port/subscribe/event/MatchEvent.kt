package com.monglife.mongs.application.battle.port.subscribe.event

import com.monglife.mongs.domain.battle.enums.MatchRoundCode

enum class MatchEventCode(
    val code: String,
    val message: String,
) {
    MATCH_PLAYERS_ENTERED("200-100-002", "모든 플레이어가 입장했습니다."),
    MATCH("200-100-003", "라운드가 종료되었습니다."),
    MATCH_END("200-100-004", "배틀이 종료되었습니다."),
}

/**
 * 매치 이벤트 수신 래핑 클래스
 */
open class MatchEvent(
    open val code: MatchEventCode,
)

/**
 * 매치 시작 이벤트
 * code: MATCH_PLAYERS_ENTERED
 */
data class MatchStartEvent(
    override val code: MatchEventCode,
    val matchId: Long,
    val round: Int,
    val isLastRound: Boolean,
    val matchPlayers: List<MatchPlayerVo>
): MatchEvent(code = code)

/**
 * 매치 라운드 종료 이벤트
 * code: MATCH
 */
data class MatchRoundOverEvent(
    override val code: MatchEventCode,
    val matchId: Long,
    val round: Int,
    val isLastRound: Boolean,
    val matchPlayers: List<MatchPlayerVo>
): MatchEvent(code = code)

/**
 * 매치 종료 비동기 업데이트 이벤트
 * code: MATCH_END
 */
data class MatchEndEvent(
    override val code: MatchEventCode,
    val matchId: Long,
    val playerId: String,
    val name: String,
    val mongCode: String,
    val mongName: String,
): MatchEvent(code = code)

/**
 * 매치 플레이어 정보 Vo
 */
data class MatchPlayerVo(
    val playerId: String,
    val deviceId: String,
    val mongCode: String,
    val mongName: String,
    val name: String,
    val hp: Double,
    val roundCode: MatchRoundCode,
)