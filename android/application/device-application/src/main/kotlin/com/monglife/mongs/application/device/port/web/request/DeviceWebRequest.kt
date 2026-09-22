package com.monglife.mongs.application.device.port.web.request

/**
 * 걸음 수 환전 요청
 */
data class ExchangeWalkingCountRequest(
    val mongId: Long,
    val walkingCount: Int,
)
