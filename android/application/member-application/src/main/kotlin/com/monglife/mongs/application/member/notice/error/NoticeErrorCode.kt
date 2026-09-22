package com.monglife.mongs.application.member.notice.error

import com.monglife.core.common.error.ErrorCode

enum class NoticeErrorCode(
    private val message: String,
    private val isMessageShow: Boolean,
) : ErrorCode {

    NOT_FOUND_NOTICE("공지 조회 실패", true)
    ;

    override fun getMessage(): String {
        return this.message
    }

    override fun isMessageShow(): Boolean {
        return this.isMessageShow
    }
}