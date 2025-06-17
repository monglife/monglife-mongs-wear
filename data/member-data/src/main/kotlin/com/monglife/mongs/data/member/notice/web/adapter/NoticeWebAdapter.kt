package com.monglife.mongs.data.member.notice.web.adapter

import com.monglife.mongs.application.member.notice.exception.NotFoundNoticeException
import com.monglife.mongs.application.member.notice.port.web.NoticeWebPort
import com.monglife.mongs.application.member.notice.port.web.response.GetNoticeResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoticeWebAdapter @Inject constructor(

) : NoticeWebPort {

    /**
     * 공지 사항 조회
     */
    @Throws(NotFoundNoticeException::class)
    override suspend fun getNotice(noticeId: Long): GetNoticeResponse {
        TODO("Not yet implemented")
    }

    /**
     * 공지 사항 목록 조회
     */
    override suspend fun getNotices(page: Int, size: Int): List<GetNoticeResponse> {
        TODO("Not yet implemented")
    }
}