package com.monglife.mongs.data.device.persistence.dto

data class DeviceEventDto(
    val deviceId: String,
    val walkingCount: Int,
    val consumeWalkingCount: Int,
)
