package com.monglife.core.billing.exception

import com.monglife.core.billing.error.BillingErrorCode
import com.monglife.core.common.error.ErrorCode
import com.monglife.core.common.exception.ErrorException

/**
 * 결제 미지원 기기 예외
 */
class BillingNotSupportException(
    override val code: ErrorCode = BillingErrorCode.NOT_SUPPORT_BILLING,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)

/**
 * 결제 시스템 연결 실패 예외
 */
class BillingConnectException(
    override val code: ErrorCode = BillingErrorCode.NOT_CONNECTING_BILLING_SYSTEM,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)

/**
 * 소비된 구글 주문 ID 목록 조회 실패 예외
 */
class InvalidGetConsumedOrdersException(
    override val code: ErrorCode = BillingErrorCode.GET_CONSUMED_ORDERS,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)

/**
 * 결제 완료 실패 예외
 */
class InvalidBillingException(
    override val code: ErrorCode = BillingErrorCode.INVALID_BILLING,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)

/**
 * 인앱 상품 중복 보유 예외
 */
class AlreadyOwnedException(
    override val code: ErrorCode = BillingErrorCode.ALREADY_OWNED,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)

/**
 * 사용자 구매 취소 예외
 */
class UserCancelException(
    override val code: ErrorCode = BillingErrorCode.USER_CANCELED,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)