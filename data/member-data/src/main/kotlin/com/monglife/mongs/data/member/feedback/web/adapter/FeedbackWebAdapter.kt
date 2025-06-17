package com.monglife.mongs.data.member.feedback.web.adapter

import com.monglife.mongs.application.member.feedback.exception.InvalidCreateFeedbackException
import com.monglife.mongs.application.member.feedback.port.web.FeedbackWebPort
import com.monglife.mongs.application.member.feedback.port.web.response.GetFeedbackTypesResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedbackWebAdapter @Inject constructor(

) : FeedbackWebPort {

    /**
     * 오류 신고 타입 목록 조회
     */
    override suspend fun getFeedbackTypes(): List<GetFeedbackTypesResponse> {
        TODO("Not yet implemented")
    }

    /**
     * 오류 신고 등록
     */
    @Throws(InvalidCreateFeedbackException::class)
    override suspend fun createFeedback(deviceName: String, title: String, content: String) {
        TODO("Not yet implemented")
    }
}