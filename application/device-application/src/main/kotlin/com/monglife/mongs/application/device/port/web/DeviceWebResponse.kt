package com.monglife.mongs.application.device.port.web

/**
 * 걸음 수 환전 응답 Vo
 */
data class ExchangeWalkingCountResponseVo(
    val consumeWalkingCount: Int,
    val walkingCount: Int,
)

/**
 * 걸음 수 동기화 응답 Vo
 */
data class UpdateWalkingCountResponseVo(
    val consumeWalkingCount: Int,
    val walkingCount: Int,
)