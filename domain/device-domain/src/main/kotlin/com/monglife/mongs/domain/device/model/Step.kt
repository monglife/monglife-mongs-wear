package com.monglife.mongs.domain.device.model

import java.time.LocalDateTime

@Suppress("NewApi")
class   Step(
    val totalWalkingCount: Int = 0,
    val deviceBootedAt: LocalDateTime = LocalDateTime.of(2000, 1, 1, 0, 0),
    walkingCount: Int = 0,
    consumedWalkingCount: Int = 0,
) {
    var walkingCount: Int = walkingCount
        private set
    var consumedWalkingCount: Int = consumedWalkingCount
        private set

    /**
     * 총 걸음 수 조회
     */
    fun getCurrentWalkingCount(): Int {
        return 0.coerceAtLeast(totalWalkingCount - consumedWalkingCount + walkingCount)
    }

    /**
     * 소비 걸음 수 동기화
     */
    fun updateWalkingCount(consumedWalkingCount: Int, walkingCount: Int) {
        this.consumedWalkingCount = consumedWalkingCount
        this.walkingCount = walkingCount
    }
}
