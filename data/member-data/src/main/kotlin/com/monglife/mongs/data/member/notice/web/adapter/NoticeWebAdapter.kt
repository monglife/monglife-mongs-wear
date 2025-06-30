package com.monglife.mongs.data.member.notice.web.adapter

import com.monglife.mongs.application.member.notice.exception.NotFoundNoticeException
import com.monglife.mongs.application.member.notice.port.web.NoticeWebPort
import com.monglife.mongs.application.member.notice.port.web.response.GetNoticeResponse
import com.monglife.mongs.core.domain.port.response.PageResponse
import com.monglife.mongs.data.member.notice.web.client.NoticeWebClient
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NoticeWebAdapterModule {
    @Binds
    @Singleton
    abstract fun bindNoticeWebPort(adapter: NoticeWebAdapter): NoticeWebPort
}

@Singleton
class NoticeWebAdapter @Inject constructor(
    private val noticeWebClient: NoticeWebClient,
) : NoticeWebPort {

    /**
     * 공지 사항 조회
     */
    @Throws(NotFoundNoticeException::class)
    override suspend fun getNotice(noticeId: Long): GetNoticeResponse =
        noticeWebClient.getNotice(noticeId = noticeId).let { response ->

            val body =
                response.takeIf { it.isSuccessful }?.body() ?: throw NotFoundNoticeException()

            GetNoticeResponse(
                noticeId = body.result.noticeId,
                title = body.result.title,
                content = body.result.content,
                writerName = body.result.writerName,
                createdAt = body.result.createdAt,
                updatedAt = body.result.updatedAt,
            )
        }

    /**
     * 공지 사항 목록 조회
     */
    override suspend fun getNotices(page: Int, size: Int): PageResponse<GetNoticeResponse> =
        noticeWebClient.getNotices(page = page, size = size).let { response ->

            val body = response.takeIf { it.isSuccessful }?.body()

            val result = body?.result?.map {
                GetNoticeResponse(
                    noticeId = it.noticeId,
                    title = it.title,
                    content = it.content,
                    writerName = it.writerName,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt,
                )
            } ?: emptyList()

            PageResponse(
                page = body?.page ?: page,
                size = body?.size ?: size,
                totalPage = body?.totalPage ?: 0,
                isLastPage = body?.isLastPage ?: true,
                result = result,
            )
        }
}