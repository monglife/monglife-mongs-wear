package com.monglife.mongs.application.member.store.exception

import com.monglife.mongs.application.member.store.error.StoreErrorCode
import com.monglife.mongs.core.domain.error.ErrorCode
import com.monglife.mongs.core.domain.exception.ErrorException

/**
 * 주문 소비 실패 예외
 */
class InvalidSubscribePlayerException(
    override val code: ErrorCode = StoreErrorCode.INVALID_CONSUME_ORDER,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)