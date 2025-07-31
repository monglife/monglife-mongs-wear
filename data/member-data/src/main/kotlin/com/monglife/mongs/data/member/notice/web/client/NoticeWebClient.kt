package com.monglife.mongs.data.member.notice.web.client

import com.monglife.core.data.web.dto.response.PageResponseDto
import com.monglife.core.data.web.dto.response.ResponseDto
import com.monglife.mongs.data.member.notice.web.client.response.GetNoticeResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface NoticeWebClient {

    /**
     * 공지 사항 조회 API 호출
     */
    @GET("user/notice/{noticeId}")
    suspend fun getNotice(@Path("noticeId") noticeId: Long): Response<ResponseDto<GetNoticeResponseDto>>

    /**
     * 공지 사항 목록 조회 API 호출
     */
    @GET("user/notice")
    suspend fun getNotices(@Query("page") page: Int, @Query("size") size: Int): Response<PageResponseDto<List<GetNoticeResponseDto>>>
}