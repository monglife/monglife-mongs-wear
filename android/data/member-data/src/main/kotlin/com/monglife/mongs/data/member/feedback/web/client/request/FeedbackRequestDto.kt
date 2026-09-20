package com.monglife.mongs.data.member.feedback.web.client.request

/**
 * 오류 신고 등록 요청 Dto
 */
data class CreateFeedbackRequestDto(
    val deviceName: String,
    val title: String,
    val content: String,
    /**
     * 신고 시점의 진단 로그. 못 모았으면 null 이고 서버도 없는 걸로 받는다 -
     * 로그가 없다고 신고가 실패하면 안 된다.
     */
    val logs: String? = null,
)