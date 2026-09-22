package com.monglife.mongs.data.auth.web.client.request

/**
 * 기기 등록 요청 Dto
 */
data class CreateDeviceRequestDto(
    val deviceId: String,
    val deviceName: String,
    val appPackageName: String,
    val fcmToken: String,
)