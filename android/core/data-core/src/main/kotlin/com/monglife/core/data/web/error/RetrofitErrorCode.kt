package com.monglife.core.data.web.error

import com.monglife.core.common.error.ErrorCode

enum class RetrofitErrorCode(
    private val message: String,
    private val isMessageShow: Boolean,
) : ErrorCode {

    INVALID_REISSUE("토큰 재발행 실패", false),
    ;

    override fun getMessage(): String {
        return this.message
    }

    override fun isMessageShow(): Boolean {
        return this.isMessageShow
    }
}