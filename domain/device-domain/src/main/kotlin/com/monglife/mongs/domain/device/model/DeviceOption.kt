package com.monglife.mongs.domain.device.model

class DeviceOption(
    backgroundMapCode: String = DEFAULT_MAP_TYPE_CODE,
    notificationOption: Boolean = false,
    soundVolume: Float = 0f,
    initNotificationDialogOpen: Boolean = true,
) {
    var backgroundMapCode: String = backgroundMapCode
        private set
    var notificationOption: Boolean = notificationOption
        private set
    var soundVolume: Float = soundVolume
        private set
    var initNotificationDialogOpen: Boolean = initNotificationDialogOpen
        private set

    companion object {
        const val DEFAULT_MAP_TYPE_CODE = "MP000"
    }

    /**
     * 맵 배경 코드 변경
     */
    fun updateBackgroundMapCode(backgroundMapCode: String) {
        this.backgroundMapCode = backgroundMapCode
    }

    /**
     * 알림 옵션 수정
     */
    fun updateNotificationOption(notificationOption: Boolean) {
        this.notificationOption = notificationOption
    }

    /**
     * 사운드 옵션 수정
     */
    fun updateSoundVolume(soundVolume: Float) {
        this.soundVolume = soundVolume
    }

    /**
     * 초기 알림 다이얼로그 오픈 여부 수정
     */
    fun updateInitNotificationDialogOpen(initNotificationDialogOpen: Boolean) {
        this.initNotificationDialogOpen = initNotificationDialogOpen
    }
}
