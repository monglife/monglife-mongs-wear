package com.monglife.mongs.data.device.web.client.request

/**
 * 걸음 수 환전 요청
 */
data class ExchangeCurrentWalkingCountRequestDto(
    val mongId: Long,
    val walkingCount: Int,
)
