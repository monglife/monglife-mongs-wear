package com.monglife.mongs.application.member.notice.error

import com.monglife.mongs.core.domain.error.ErrorCode

enum class NoticeErrorCode(
    private val message: String,
    private val isMessageShow: Boolean,
) : ErrorCode {

    NOT_FOUND_NOTICE("공지 사항 조회 실패", false)
    ;

    override fun getMessage(): String {
        return this.message
    }

    override fun isMessageShow(): Boolean {
        return this.isMessageShow
    }
}