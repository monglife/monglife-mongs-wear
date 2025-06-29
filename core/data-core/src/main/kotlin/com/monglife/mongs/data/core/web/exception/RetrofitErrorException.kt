package com.monglife.mongs.data.core.web.exception

import com.monglife.core.common.error.ErrorCode
import com.monglife.core.common.exception.ErrorException
import com.monglife.mongs.data.core.web.error.RetrofitErrorCode

/**
 * 토큰 재발행 실패 예외
 */
class InvalidReissueException(
    override val code: ErrorCode = RetrofitErrorCode.INVALID_REISSUE,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)
