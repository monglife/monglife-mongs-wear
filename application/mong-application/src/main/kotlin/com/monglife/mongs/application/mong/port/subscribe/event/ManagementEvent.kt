package com.monglife.mongs.application.mong.port.subscribe.event

enum class ManagementEventCode(
    val code: String,
    val message: String,
) {

    ;
}

/**
 * Management 이벤트 수신 래핑 클래스
 */
open class ManagementEvent(
    open val code: ManagementEventCode,
)

/**
 * 몽 비동기 업데이트 이벤트
 */
data class MongUpdateEvent(
    override val code: ManagementEventCode,
): ManagementEvent(code = code)