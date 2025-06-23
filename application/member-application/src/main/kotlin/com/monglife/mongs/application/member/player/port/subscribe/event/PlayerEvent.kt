package com.monglife.mongs.application.member.player.port.subscribe.event

enum class PlayerEventCode(
    val code: String,
    val message: String,
) {

    ;
}

/**
 * Player 이벤트 수신 래핑 클래스
 */
open class PlayerEvent(
    open val code: PlayerEventCode,
)

/**
 * 플레이어 비동기 업데이트 이벤트
 */
data class PlayerUpdateEvent(
    override val code: PlayerEventCode,
): PlayerEvent(code = code)