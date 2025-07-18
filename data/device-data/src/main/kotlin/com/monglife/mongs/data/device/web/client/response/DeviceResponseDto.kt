package com.monglife.mongs.data.device.web.client.response

/**
 * 걸음 수 조회 응답 Dto
 */
data class GetStepResponseDto(
    val consumeWalkingCount: Int,
    val walkingCount: Int,
)

/**
 * 걸음 수 환전 응답 Dto
 */
data class ExchangeCurrentWalkingCountResponseDto(
    val consumeWalkingCount: Int,
    val walkingCount: Int,
)

/**
 * 걸음 수 동기화 응답 Dto
 */
data class UpdateTotalWalkingCountResponseDto(
    val consumeWalkingCount: Int,
    val walkingCount: Int,
)