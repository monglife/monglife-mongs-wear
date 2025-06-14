package com.monglife.mongs.application.member.feedback.port.web.response

import com.monglife.mongs.domain.member.feedback.model.FeedbackType

/**
 * 오류 신고 타입 조회 응답
 */
data class GetFeedbackTypesResponse(
    val feedbackTypeId: Long,
    val feedbackName: String,
    val description: String,
) {
    /**
     * 응답 도메인 변환
     */
    fun toDomain(): FeedbackType {
        return FeedbackType(
            feedbackTypeId = this.feedbackTypeId,
            feedbackName = this.feedbackName,
            description = this.description,
        )
    }
}
