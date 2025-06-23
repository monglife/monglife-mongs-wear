package com.monglife.mongs.data.member.player.web.client.response

/**
 * 플레이어 조회 응답 Dto
 */
data class GetPlayerResponseDto(
    val accountId: Long,
    val slotCount: Int,
    val starPoint: Int,
)

/**
 * 슬롯 구매 응답 Dto
 */
data class BuySlotResponseDto(
    val accountId: Long,
    val slotCount: Int,
    val starPoint: Int,
)

/**
 * 스타 포인트 환전 응답 Dto
 */
data class ExchangeStarPointResponseDto(
    val accountId: Long,
    val starPoint: Int,
)