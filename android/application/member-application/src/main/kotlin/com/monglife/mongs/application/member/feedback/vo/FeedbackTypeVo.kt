package com.monglife.mongs.application.member.feedback.vo

import com.monglife.mongs.domain.member.feedback.model.FeedbackType

data class FeedbackTypeVo(
    val feedbackTypeId: Long,
    val feedbackName: String,
    val description: String,
) {
    companion object {

        /**
         * 도메인 Vo 변환
         */
        fun of(feedbackType: FeedbackType) = FeedbackTypeVo(
            feedbackTypeId = feedbackType.feedbackTypeId,
            feedbackName = feedbackType.feedbackName,
            description = feedbackType.description,
        )
    }
}
