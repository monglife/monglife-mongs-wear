package com.monglife.mongs.data.core.mqtt.error

import com.monglife.core.common.error.ErrorCode

enum class MqttErrorCode(
    private val message: String,
    private val isMessageShow: Boolean,
) : ErrorCode {

    INVALID_CONNECT("연결 실패", false),
    INVALID_DIS_CONNECT("연결 해제 실패", false),
    INVALID_SUBSCRIBE("구독 실패", false),
    INVALID_DIS_SUBSCRIBE("구독 해제 실패", false),
    INVALID_PUBLISH("전송 실패", false),
    UNKNOWN("알 수 없는 에러", false),
    ;

    override fun getMessage(): String {
        return this.message
    }

    override fun isMessageShow(): Boolean {
        return this.isMessageShow
    }
}