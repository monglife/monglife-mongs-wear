package com.monglife.mongs.application.device.error

import com.monglife.core.common.error.ErrorCode

enum class DeviceErrorCode(
    private val message: String,
    private val isMessageShow: Boolean,
) : ErrorCode {

    EXCHANGE_WALKING_COUNT("걸음 수 환전 실패", true),
    ;

    override fun getMessage(): String {
        return this.message
    }

    override fun isMessageShow(): Boolean {
        return this.isMessageShow
    }
}
