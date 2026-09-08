package com.monglife.mongs.application.auth.port.web.request

/**
 * 기기 등록 요청
 */
data class CreateDeviceRequest(
    val deviceId: String,
    val deviceName: String,
    val appPackageName: String,
    val fcmToken: String,
)