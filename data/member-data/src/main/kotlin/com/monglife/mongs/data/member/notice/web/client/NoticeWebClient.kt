package com.monglife.mongs.data.member.notice.web.client

import com.monglife.mongs.data.core.web.dto.response.PageResponseDto
import com.monglife.mongs.data.core.web.dto.response.ResponseDto
import com.monglife.mongs.data.member.notice.web.client.response.GetNoticeResponseDto
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NoticeWebClientModule {
    @Provides
    @Singleton
    fun provideNoticeWebClient(
        @Named("monglife-mongs") retrofit: Retrofit
    ): NoticeWebClient = retrofit.create(NoticeWebClient::class.java)
}

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