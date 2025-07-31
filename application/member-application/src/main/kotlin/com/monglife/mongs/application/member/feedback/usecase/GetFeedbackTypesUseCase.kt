package com.monglife.mongs.application.member.feedback.usecase

import com.monglife.core.application.usecase.BaseNoParamUseCase
import com.monglife.mongs.application.member.feedback.port.web.FeedbackWebPort
import com.monglife.mongs.application.member.feedback.vo.FeedbackTypeVo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 오류 신고 타입 목록 조회 UseCase
 */
class GetFeedbackTypesUseCase @Inject constructor(
    private val feedbackWebPort: FeedbackWebPort,
) : BaseNoParamUseCase<List<FeedbackTypeVo>>() {

    override suspend fun execute(): List<FeedbackTypeVo> {
        return withContext(Dispatchers.IO) {
            // 오류 신고 타입 목록 조회 요청
            feedbackWebPort.getFeedbackTypes().map { response ->
                // FeedbackTypeVo 반환
                FeedbackTypeVo.of(feedbackType = response.toDomain())
            }
        }
    }
}