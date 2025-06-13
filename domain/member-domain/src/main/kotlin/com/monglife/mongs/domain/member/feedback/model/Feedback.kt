package com.monglife.mongs.domain.member.feedback.model

class Feedback(
    feedbackId: Long,
    accountId: Long,
    deviceId: String,
    deviceName: String,
    title: String,
    content: String,
) {

    var feedbackId: Long = feedbackId
        private set
    var accountId: Long = accountId
        private set
    var deviceId: String = deviceId
        private set
    var deviceName: String = deviceName
        private set
    var title: String = title
        private set
    var content: String = content
        private set
}