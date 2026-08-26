package com.monglife.mongs.data.device.persistence.dto

/**
 * 서버가 보내는 걸음 수 복구 알림
 *
 * 환전 요청은 성공했지만 그 뒤 페이 포인트 지급이 실패했을 때 온다.
 * 잔액은 기기에만 있으므로 서버는 되돌릴 금액만 알려 줄 수 있다.
 */
data class StepRestoreEventDto(
    val deviceId: String,
    val restoreWalkingCount: Int,

    /**
     * 서버 롤백 이벤트의 transactionId. 같은 복구가 재전달되어도 값이 같다.
     */
    val eventId: String,
)
