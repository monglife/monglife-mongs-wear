package com.monglife.mongs.application.member.feedback.exception

import com.monglife.mongs.application.member.feedback.error.FeedbackErrorCode
import com.monglife.core.common.error.ErrorCode
import com.monglife.core.common.exception.ErrorException

/**
 * 오류 신고 등록 실패 예외
 */
class InvalidCreateFeedbackException(
    override val code: ErrorCode = FeedbackErrorCode.INVALID_CREATE_FEEDBACK,
    override val message: String = code.getMessage()
) : ErrorException(code = code, message = message)