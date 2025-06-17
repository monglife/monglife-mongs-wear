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
 * 매치 플레이어 정보 비동기 업데이트 Dto
 */
class UpdateMatchPlayerDto(
    val playerId: String,
    val deviceId: String,
    val mongCode: String,
    val mongName: String,
    val hp: Double,
    val roundCode: MatchRoundCode,
)

/**
 * 라운드 종료 매치 비동기 업데이트 Dto
 */
class FightMatchDto(
    override val code: MatchEventCode,
    val matchId: Long,
    val round: Int,
    val isLastRound: Boolean,
    val matchPlayers: List<UpdateMatchPlayerDto>
): MatchEvent(code = code)