package com.monglife.core.data.mqtt.exception

import com.monglife.core.common.error.ErrorCode
import com.monglife.core.common.exception.ErrorException
import com.monglife.core.data.mqtt.error.MqttErrorCode

/**
 * 연결 실패 예외
 */
class InvalidConnectException(
    override val code: ErrorCode = MqttErrorCode.INVALID_CONNECT,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)

/**
 * 연결 해제 실패 예외
 */
class InvalidDisConnectException(
    override val code: ErrorCode = MqttErrorCode.INVALID_DIS_CONNECT,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)

/**
 * 구독 실패 예외
 */
class InvalidSubscribeException(
    override val code: ErrorCode = MqttErrorCode.INVALID_SUBSCRIBE,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)

/**
 * 구독 해제 실패 예외
 */
class InvalidDisSubscribeException(
    override val code: ErrorCode = MqttErrorCode.INVALID_DIS_SUBSCRIBE,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)

/**
 * 전송 실패 예외
 */
class InvalidPublishException(
    override val code: ErrorCode = MqttErrorCode.INVALID_PUBLISH,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)

/**
 * 알 수 없는 에러
 */
class UnKnownException(
    override val code: ErrorCode = MqttErrorCode.UNKNOWN,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)