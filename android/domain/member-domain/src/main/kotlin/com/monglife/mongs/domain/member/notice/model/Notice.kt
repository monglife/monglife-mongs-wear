package com.monglife.mongs.domain.member.notice.model

import java.time.LocalDateTime

class Notice (
    noticeId: Long,
    title: String,
    content: String,
    writerName: String,
    createdAt: LocalDateTime,
    updatedAt: LocalDateTime,
) {
    var noticeId: Long = noticeId
        private set
    var title: String = title
        private set
    var content: String = content
        private set
    var writerName: String = writerName
        private set
    var createdAt: LocalDateTime = createdAt
        private set
    var updatedAt: LocalDateTime = updatedAt
        private set
}