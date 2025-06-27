package com.monglife.mongs.application.member.store.exception

import com.monglife.mongs.application.member.store.error.StoreErrorCode
import com.monglife.core.common.error.ErrorCode
import com.monglife.core.common.exception.ErrorException

/**
 * 주문 소비 실패 예외
 */
class InvalidConsumeOrderException(
    override val code: ErrorCode = StoreErrorCode.INVALID_CONSUME_ORDER,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)

/**
 * 소비 주문 조회 실패 예외
 */
class InvalidGetConsumedOrdersException(
    override val code: ErrorCode = StoreErrorCode.GET_CONSUMED_ORDERS,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)

/**
 * 지원 기기 아님 예외
 */
class BillingNotSupportException(
    override val code: ErrorCode = StoreErrorCode.BILLING_NOT_SUPPORT,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)
