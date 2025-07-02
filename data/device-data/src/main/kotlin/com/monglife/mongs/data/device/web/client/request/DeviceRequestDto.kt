package com.monglife.mongs.data.device.web.client.request

import java.time.LocalDateTime

/**
 * 걸음 수 환전 요청 Dto
 */
data class ExchangeCurrentWalkingCountRequestDto(
    val mongId: Long,
    val totalWalkingCount: Int,
    val walkingCount: Int,
    val deviceBootedAt: LocalDateTime,
)

/**
 * 걸음 수 동기화 요청 Dto
 */
data class UpdateTotalWalkingCountRequestDto(
    val totalWalkingCount: Int,
    val deviceBootedAt: LocalDateTime,
)