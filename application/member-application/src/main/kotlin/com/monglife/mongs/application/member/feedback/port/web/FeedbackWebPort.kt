package com.monglife.mongs.application.member.feedback.port.web

import com.monglife.mongs.application.member.feedback.exception.InvalidCreateFeedbackException
import com.monglife.mongs.application.member.feedback.port.web.response.GetFeedbackTypeResponse

interface FeedbackWebPort {

    /**
     * 오류 신고 타입 목록 조회
     */
    suspend fun getFeedbackTypes(): List<GetFeedbackTypeResponse>

    /**
     * 오류 신고 등록
     */
    @Throws(InvalidCreateFeedbackException::class)
    suspend fun createFeedback(deviceName: String, title: String, content: String)
}