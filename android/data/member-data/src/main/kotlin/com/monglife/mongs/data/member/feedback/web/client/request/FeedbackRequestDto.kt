package com.monglife.mongs.data.member.feedback.web.client.request

/**
 * 오류 신고 등록 요청 Dto
 */
data class CreateFeedbackRequestDto(
    val deviceName: String,
    val title: String,
    val content: String,
)