package com.monglife.mongs.data.member.feedback.web.adapter

import com.monglife.mongs.application.member.feedback.exception.InvalidCreateFeedbackException
import com.monglife.mongs.application.member.feedback.port.web.FeedbackWebPort
import com.monglife.mongs.application.member.feedback.port.web.response.GetFeedbackTypeResponse
import com.monglife.mongs.data.member.feedback.web.client.FeedbackWebClient
import com.monglife.mongs.data.member.feedback.web.client.request.CreateFeedbackRequestDto
import com.monglife.mongs.data.member.feedback.web.enums.FeedbackTypeCode
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedbackWebAdapter @Inject constructor(
    private val feedbackWebClient: FeedbackWebClient,
) : FeedbackWebPort {

    /**
     * 오류 신고 타입 목록 조회
     */
    override suspend fun getFeedbackTypes(): List<GetFeedbackTypeResponse> =
        FeedbackTypeCode.entries.map {
            GetFeedbackTypeResponse(
                feedbackTypeId = it.feedbackTypeId,
                feedbackName = it.feedbackName,
                description = it.description,
            )
        }

    /**
     * 오류 신고 등록
     */
    @Throws(InvalidCreateFeedbackException::class)
    override suspend fun createFeedback(deviceName: String, title: String, content: String) {
        feedbackWebClient.createFeedback(
            createFeedbackRequestDto = CreateFeedbackRequestDto(
                deviceName = deviceName,
                title = title,
                content = content,
            )
        ).let { it.takeIf { it.isSuccessful } ?: throw InvalidCreateFeedbackException() }
    }
}