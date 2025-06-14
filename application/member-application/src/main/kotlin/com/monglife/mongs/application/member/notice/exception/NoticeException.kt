package com.monglife.mongs.application.member.notice.exception

import com.monglife.mongs.application.member.notice.error.NoticeErrorCode
import com.monglife.mongs.core.domain.error.ErrorCode
import com.monglife.mongs.core.domain.exception.ErrorException

/**
 * 공지 사항 조회 실패 예외
 */
class NotFoundNoticeException(
    override val code: ErrorCode = NoticeErrorCode.NOT_FOUND_NOTICE,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)