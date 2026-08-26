package com.monglife.mongs.domain.device.model

/**
 * 환전 실패분 복구 판정
 *
 * 서버가 보내는 복구 알림은 MQTT 라 같은 알림이 두 번 도착할 수 있다. 이미 반영한 알림을
 * 다시 적립하지 않도록 걸러 내는 규칙이다.
 */
object StepRestore {

    /**
     * 보관할 복구 이력 개수.
     *
     * 이력은 중복 판정에만 쓰이므로 무한정 쌓을 이유가 없다. 복구는 환전 실패라는 드문 사건에만
     * 발생하므로 이 정도면 재전달 창을 충분히 덮는다.
     */
    const val MAX_APPLIED_EVENT_IDS = 50

    /**
     * @param appliedEventIds 이미 반영한 복구 식별자 (오래된 것부터)
     * @return [StepRestoreResult.restoredWalkingCount] 가 0 이면 반영하지 않는다는 뜻이다.
     */
    fun apply(
        appliedEventIds: List<String>,
        restoreWalkingCount: Int,
        eventId: String,
    ): StepRestoreResult {

        val ignored = StepRestoreResult(restoredWalkingCount = 0, appliedEventIds = appliedEventIds)

        // 식별자가 없으면 중복을 가려낼 수 없다. 이중 적립보다는 반영하지 않는 쪽이 안전하다.
        if (eventId.isBlank()) return ignored

        if (restoreWalkingCount <= 0) return ignored

        if (appliedEventIds.contains(eventId)) return ignored

        return StepRestoreResult(
            restoredWalkingCount = restoreWalkingCount,
            appliedEventIds = (appliedEventIds + eventId).takeLast(MAX_APPLIED_EVENT_IDS),
        )
    }
}

data class StepRestoreResult(
    val restoredWalkingCount: Int,
    val appliedEventIds: List<String>,
)
