package com.monglife.mongs.domain.auth.model

class UserDevice(
    deviceId: String,
    deviceName: String,
    appPackageName: String,
    fcmToken: String,
) {
    var deviceId: String = deviceId
        private set
    var deviceName: String = deviceName
        private set
    var appPackageName: String = appPackageName
        private set
    var fcmToken: String = fcmToken
        private set}
