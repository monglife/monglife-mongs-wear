package com.monglife.mongs.data.member.feedback.web.client

import com.monglife.core.data.web.dto.response.ResponseDto
import com.monglife.mongs.data.member.feedback.web.client.request.CreateFeedbackRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface FeedbackWebClient {

    /**
     * 오류 신고 등록 API 호출
     */
    @POST("user/feedback")
    suspend fun createFeedback(@Body createFeedbackRequestDto: CreateFeedbackRequestDto): Response<ResponseDto<Void>>
}