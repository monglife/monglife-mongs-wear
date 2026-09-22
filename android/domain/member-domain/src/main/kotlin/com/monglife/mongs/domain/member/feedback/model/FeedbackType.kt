package com.monglife.mongs.domain.member.feedback.model

class FeedbackType(
    feedbackTypeId: Long,
    feedbackName: String,
    description: String,
) {
    var feedbackTypeId: Long = feedbackTypeId
        private set
    var feedbackName: String = feedbackName
        private set
    var description: String = description
        private set
}