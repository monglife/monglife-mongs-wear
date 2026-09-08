package com.monglife.mongs.application.device.exception

import com.monglife.core.common.error.ErrorCode
import com.monglife.core.common.exception.ErrorException
import com.monglife.mongs.application.device.error.DeviceErrorCode

/**
 * 걸음 수 환전 실패 예외
 *
 * 잔액 부족과 서버 실패를 같은 예외로 묶는다. 사용자 입장에서는 둘 다 "환전이 안 됐다" 이고,
 * 잔액 부족은 UI 가 애초에 막고 있어 여기까지 오는 경우가 드물다.
 */
class InvalidExchangeWalkingCountException(
    override val code: ErrorCode = DeviceErrorCode.EXCHANGE_WALKING_COUNT,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)
