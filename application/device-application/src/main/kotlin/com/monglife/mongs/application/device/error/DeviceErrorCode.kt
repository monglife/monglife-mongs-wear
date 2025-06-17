package com.monglife.mongs.application.device.error

import com.monglife.mongs.core.domain.error.ErrorCode

enum class DeviceErrorCode(
    private val message: String,
    private val isMessageShow: Boolean,
) : ErrorCode {

    EXCHANGE_WALKING_COUNT("걸음 수 환전 실패", false),
    UPDATE_WALKING_COUNT("걸음 수 서버 동기화 실패", false),
    NOT_FOUND_DEVICE_OPTION("DeviceOption 조회 실패", false),
    ;

    override fun getMessage(): String {
        return this.message
    }

    override fun isMessageShow(): Boolean {
        return this.isMessageShow
    }
}