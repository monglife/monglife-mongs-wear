package com.monglife.mongs.application.member.feedback.error

import com.monglife.mongs.core.domain.error.ErrorCode

enum class FeedbackErrorCode(
    private val message: String,
    private val isMessageShow: Boolean,
) : ErrorCode {

    INVALID_CREATE_FEEDBACK("오류 신고 실패", false),
    ;

    override fun getMessage(): String {
        return this.message
    }

    override fun isMessageShow(): Boolean {
        return this.isMessageShow
    }
}