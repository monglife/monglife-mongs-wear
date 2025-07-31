package com.monglife.mongs.application.device.port.web.response

/**
 * 걸음 수 조회 응답
 */
data class GetStepResponse(
    val consumeWalkingCount: Int,
    val walkingCount: Int,
)

/**
 * 걸음 수 환전 응답
 */
data class ExchangeWalkingCountResponse(
    val consumeWalkingCount: Int,
    val walkingCount: Int,
)

/**
 * 걸음 수 동기화 응답
 */
data class UpdateWalkingCountResponse(
    val consumeWalkingCount: Int,
    val walkingCount: Int,
)