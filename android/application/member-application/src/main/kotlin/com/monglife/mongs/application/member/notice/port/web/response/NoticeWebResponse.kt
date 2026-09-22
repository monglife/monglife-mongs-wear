package com.monglife.mongs.application.member.notice.port.web.response

import com.monglife.mongs.domain.member.notice.model.Notice
import java.time.LocalDateTime

data class GetNoticeResponse(
    val noticeId: Long,
    val title: String,
    val content: String,
    val writerName: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {

    /**
     * 응답 도메인 변환
     */
    fun toDomain() = Notice(
        noticeId = this.noticeId,
        title = this.title,
        content = this.content,
        writerName = this.writerName,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
    )
}