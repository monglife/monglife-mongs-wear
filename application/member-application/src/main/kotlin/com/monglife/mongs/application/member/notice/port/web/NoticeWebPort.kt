package com.monglife.mongs.application.member.notice.port.web

import com.monglife.mongs.application.member.notice.exception.NotFoundNoticeException
import com.monglife.mongs.application.member.notice.port.web.response.GetNoticeResponse

interface NoticeWebPort {

    /**
     * 공지 사항 조회
     */
    @Throws(NotFoundNoticeException::class)
    suspend fun getNotice(noticeId: Long): GetNoticeResponse

    /**
     * 공지 사항 목록 조회
     */
    suspend fun getNotices(page: Int, size: Int): List<GetNoticeResponse>
}