package com.monglife.core.billing.error

import com.monglife.core.common.error.ErrorCode

enum class BillingErrorCode(
    private val message: String,
    private val isMessageShow: Boolean,
) : ErrorCode {

    NOT_SUPPORT_BILLING("결제 미지원 기기", true),
    NOT_CONNECTING_BILLING_SYSTEM("구글 결제 시스템 연결 실패", false),
    GET_CONSUMED_ORDERS("소비된 구글 주문 ID 목록 조회 실패", false),
    INVALID_BILLING("결제 완료 실패", true),
    ALREADY_OWNED("이미 보유중인 아이템", true),
    USER_CANCELED("구매 취소", true),
    ;

    override fun getMessage(): String {
        return this.message
    }

    override fun isMessageShow(): Boolean {
        return this.isMessageShow
    }
}