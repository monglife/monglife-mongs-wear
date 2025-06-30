package com.monglife.mongs.data.member.feedback.web.client

import com.monglife.mongs.data.core.web.dto.response.ResponseDto
import com.monglife.mongs.data.member.feedback.web.client.request.CreateFeedbackRequestDto
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.POST
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FeedbackWebClientModule {
    @Provides
    @Singleton
    fun provideFeedbackWebClient(
        @Named("monglife-mongs") retrofit: Retrofit
    ): FeedbackWebClient = retrofit.create(FeedbackWebClient::class.java)
}

interface FeedbackWebClient {

    /**
     * 오류 신고 등록 API 호출
     */
    @POST("user/feedback")
    suspend fun createFeedback(@Body createFeedbackRequestDto: CreateFeedbackRequestDto): Response<ResponseDto<Void>>
}