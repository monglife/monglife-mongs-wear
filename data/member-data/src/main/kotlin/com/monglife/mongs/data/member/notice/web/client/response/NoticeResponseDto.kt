package com.monglife.mongs.data.member.notice.web.client.response

import java.time.LocalDateTime

/**
 * 공지 사항 조회 응답 Dto
 */
data class GetNoticeResponseDto(
    val noticeId: Long,
    val title: String,
    val content: String,
    val writerName: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)