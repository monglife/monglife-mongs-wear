package com.monglife.mongs.application.battle.port.subscribe.event

enum class QueueEventCode(
    val code: String,
    val message: String,
) {
    MATCHING_QUEUE_PLAYER("200-100-000", "배틀 매칭에 성공했습니다."),
    MATCHING_QUEUE_PLAYER_FAIL("200-100-001", "배틀 매칭에 실패했습니다."),
}

/**
 * 매치 큐 이벤트 수신 래핑 클래스
 */
open class QueueEvent(
    open val code: QueueEventCode,
)

/**
 * 매치 큐 매칭 성공 이벤트
 * code: MATCHING_QUEUE_PLAYER
 */
data class MatchingEvent(
    override val code: QueueEventCode,
    val matchId: Long,
    val matchPlayers: List<QueuePlayerVo>
) : QueueEvent(code = code) {
    /**
     * 매치 큐 플레이어 정보 비동기 업데이트 Dto
     */
    data class QueuePlayerVo(
        val deviceId: String,
        val playerId: String,
        val mongId: Long,
        val mongCode: String,
        val mongName: String,
        val name: String,
    )
}

/**
 * 매치 큐 실패 이벤트
 * code: MATCHING_QUEUE_PLAYER_FAIL
 */
data class MatchingFailEvent(
    override val code: QueueEventCode,
    val deviceId: String,
): QueueEvent(code = code)