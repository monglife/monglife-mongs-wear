package com.monglife.mongs.application.device.error

import com.monglife.core.common.error.ErrorCode

enum class DeviceErrorCode(
    private val message: String,
    private val isMessageShow: Boolean,
) : ErrorCode {

    NOT_FOUND_STEP("걸음 수 조회 실패", false),
    EXCHANGE_WALKING_COUNT("걸음 수 환전 실패", true),
    UPDATE_WALKING_COUNT("걸음 수 서버 동기화 실패", false),
    ;

    override fun getMessage(): String {
        return this.message
    }

    override fun isMessageShow(): Boolean {
        return this.isMessageShow
    }
}