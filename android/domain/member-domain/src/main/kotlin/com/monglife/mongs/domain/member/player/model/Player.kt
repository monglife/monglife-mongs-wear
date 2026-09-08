package com.monglife.mongs.domain.member.player.model

class Player(
    accountId: Long,
    slotCount: Int,
    starPoint: Int,
) {
    var accountId: Long = accountId
        private set
    var slotCount: Int = slotCount
        private set
    var starPoint: Int = starPoint
        private set

    /**
     * 슬롯 구매
     */
    fun buySlot(slotCount: Int, starPoint: Int) {
        this.slotCount = slotCount
        this.starPoint = starPoint
    }

    /**
     * 스타 포인트 환전
     */
    fun exchangeStarPoint(
        starPoint: Int,
    ) {
        this.starPoint = starPoint
    }

    /**
     * 동기화
     */
    fun update(
        slotCount: Int,
        starPoint: Int,
    ) {
        this.slotCount = slotCount
        this.starPoint = starPoint
    }
}
