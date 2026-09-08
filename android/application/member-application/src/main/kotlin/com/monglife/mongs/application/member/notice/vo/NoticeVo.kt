package com.monglife.mongs.application.member.notice.vo

import com.monglife.mongs.domain.member.notice.model.Notice
import java.time.LocalDateTime

data class NoticeVo(
    val noticeId: Long,
    val title: String,
    val content: String,
    val writerName: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {

        /**
         * 도메인 Vo 변환
         */
        fun of(notice: Notice) = NoticeVo(
            noticeId = notice.noticeId,
            title = notice.title,
            content = notice.content,
            writerName = notice.writerName,
            createdAt = notice.createdAt,
            updatedAt = notice.updatedAt,
        )
    }
}
