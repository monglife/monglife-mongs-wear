package com.monglife.mongs.application.member.player.port.web.response

import com.monglife.mongs.domain.member.player.model.Player

/**
 * 플레이어 조회 응답
 */
data class GetPlayerResponse(
    val accountId: Long,
    val slotCount: Int,
    val starPoint: Int,
) {
    /**
     * 응답 도메인 변환
     */
    fun toDomain() = Player(
        accountId = this.accountId,
        slotCount = this.slotCount,
        starPoint = this.starPoint
    )
}

/**
 * 슬롯 구매 응답
 */
data class BuySlotResponse(
    val accountId: Long,
    val slotCount: Int,
    val starPoint: Int,
)

/**
 * 스타 포인트 환전 응답
 */
data class ExchangeStarPointResponse(
    val accountId: Long,
    val starPoint: Int,
)