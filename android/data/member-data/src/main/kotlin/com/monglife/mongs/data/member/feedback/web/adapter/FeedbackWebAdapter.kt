package com.monglife.mongs.data.member.feedback.web.adapter

import com.monglife.mongs.application.member.feedback.exception.InvalidCreateFeedbackException
import com.monglife.mongs.application.member.feedback.port.web.FeedbackWebPort
import com.monglife.mongs.application.member.feedback.port.web.response.GetFeedbackTypeResponse
import com.monglife.mongs.data.member.feedback.web.client.FeedbackWebClient
import com.monglife.mongs.data.member.feedback.web.client.request.CreateFeedbackRequestDto
import com.monglife.mongs.data.member.feedback.web.enums.FeedbackTypeCode
import javax.inject.Inject
import javax.inject.Singleton
import com.monglife.core.data.global.DiagnosticLog

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
    /**
     * 진단 로그는 여기서 붙인다. 애플리케이션 계층이 logcat 을 알 이유가 없고,
     * 이건 "무엇을 신고하는가" 가 아니라 "어떻게 실어 보내는가" 에 속한다.
     */
    @Throws(InvalidCreateFeedbackException::class)
    override suspend fun createFeedback(deviceName: String, title: String, content: String) {
        feedbackWebClient.createFeedback(
            createFeedbackRequestDto = CreateFeedbackRequestDto(
                deviceName = deviceName,
                title = title,
                content = content,
                logs = DiagnosticLog.collect(),
            )
        ).let { it.takeIf { it.isSuccessful } ?: throw InvalidCreateFeedbackException() }
    }
}