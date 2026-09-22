package com.monglife.mongs.data.member.player.web.client.request

/**
 * 스타 포인트 환전 요청 Dto
 */
data class ExchangeStartPointRequestDto(
    val mongId: Long,
    val starPoint: Int,
)