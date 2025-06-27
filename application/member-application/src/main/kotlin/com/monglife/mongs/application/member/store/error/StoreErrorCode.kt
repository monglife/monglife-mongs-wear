package com.monglife.mongs.application.member.store.error

import com.monglife.core.common.error.ErrorCode

enum class StoreErrorCode(
    private val message: String,
    private val isMessageShow: Boolean,
) : ErrorCode {

    INVALID_CONSUME_ORDER("주문 소비 실패", false),
    GET_CONSUMED_ORDERS("소비 주문 조회 실패", false),
    BILLING_NOT_SUPPORT("결제 미지원 기기", false),
    ;

    override fun getMessage(): String {
        return this.message
    }

    override fun isMessageShow(): Boolean {
        return this.isMessageShow
    }
}