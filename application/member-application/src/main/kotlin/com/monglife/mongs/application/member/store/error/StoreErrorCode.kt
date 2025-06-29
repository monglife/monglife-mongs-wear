package com.monglife.mongs.application.member.store.error

import com.monglife.core.common.error.ErrorCode

enum class StoreErrorCode(
    private val message: String,
    private val isMessageShow: Boolean,
) : ErrorCode {

    INVALID_CONSUME_ORDER("주문 소비 실패", false),
    ;

    override fun getMessage(): String {
        return this.message
    }

    override fun isMessageShow(): Boolean {
        return this.isMessageShow
    }
}