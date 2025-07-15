package com.monglife.mongs.data.device.persistence.dto

/**
 * 기기 정보 변경 이벤트 Dto
 */
data class DeviceEventDto(
    val deviceId: String,
    val walkingCount: Int,
    val consumeWalkingCount: Int,
)
