package com.monglife.mongs.data.device.persistence.entity

import com.monglife.mongs.domain.device.model.DeviceOption

data class DeviceOptionEntity(
    val currentMongId: Long?,
    val backgroundMapCode: String?,
    val notificationOption: Boolean,
    val soundVolume: Float,
    val initNotificationDialogOpen: Boolean,
) {
    /**
     * 엔티티 도메인 변환
     */
    fun toDomain(): DeviceOption = DeviceOption(
        currentMongId = this.currentMongId,
        backgroundMapCode = this.backgroundMapCode,
        notificationOption = this.notificationOption,
        soundVolume = this.soundVolume,
        initNotificationDialogOpen = this.initNotificationDialogOpen,
    )
}
