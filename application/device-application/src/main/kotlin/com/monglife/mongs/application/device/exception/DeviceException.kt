package com.monglife.mongs.application.device.exception

import com.monglife.mongs.application.device.error.DeviceErrorCode
import com.monglife.mongs.core.domain.error.ErrorCode
import com.monglife.mongs.core.domain.exception.ErrorException

/**
 * 걸음 수 환전 실패 예외
 */
class ExchangeWalkingCountException(
    override val code: ErrorCode = DeviceErrorCode.EXCHANGE_WALKING_COUNT,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)

/**
 * 걸음 수 동기화 실패 예외
 */
class UpdateWalkingCountException(
    override val code: ErrorCode = DeviceErrorCode.UPDATE_WALKING_COUNT,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)

/**
 * Step 조회 실패 예외
 */
class NotFoundStepException(
    override val code: ErrorCode = DeviceErrorCode.NOT_FOUND_STEP,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)

/**
 * DeviceOption 조회 실패 예외
 */
class NotFoundDeviceOptionException(
    override val code: ErrorCode = DeviceErrorCode.NOT_FOUND_DEVICE_OPTION,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)