package com.monglife.mongs.domain.device.model

class DeviceOption(
    currentMongId: Long?,
    backgroundMapCode: String?,
    notificationOption: Boolean,
    soundVolume: Float,
    initGuideOpen: Boolean,
) {
    var currentMongId: Long? = currentMongId
        private set
    var backgroundMapCode: String? = backgroundMapCode
        private set
    var notificationOption: Boolean = notificationOption
        private set
    var soundVolume: Float = soundVolume
        private set
    var initGuideOpen: Boolean = initGuideOpen
        private set

    /**
     * 맵 배경 코드 삭제
     */
    fun deleteBackgroundMapCode() {
        this.backgroundMapCode = null
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
     * 최초 가이드 표시 여부 수정
     */
    fun updateInitGuideOpen(initGuideOpen: Boolean) {
        this.initGuideOpen = initGuideOpen
    }
}
