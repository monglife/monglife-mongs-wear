package com.monglife.mongs.application.device.port.web.request

import java.time.LocalDateTime

/**
 * 걸음 수 환전 요청
 */
data class ExchangeWalkingCountRequest(
    val mongId: Long,
    val walkingCount: Int,
    val totalWalkingCount: Int,
    val deviceBootedAt: LocalDateTime,
)

/**
 * 걸음 수 서버 동기화 요청
 */
data class UpdateWalkingCountRequest(
    val deviceBootedAt: LocalDateTime,
)