package com.monglife.mongs.domain.member.feedback.usecase

import com.monglife.mongs.domain.member.feedback.repository.FeedbackRepository
import com.mongs.wear.core.exception.data.CreateFeedbackException
import com.mongs.wear.core.exception.global.DataException
import com.mongs.wear.core.exception.usecase.CreateFeedbackUseCaseException
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 오류 신고 등록 UseCase
 */
class CreateFeedbackUseCase @Inject constructor(
    private val feedbackRepository: FeedbackRepository,
) : BaseParamUseCase<CreateFeedbackUseCase.Param, Unit>() {

    override suspend fun execute(param: Param) {
        withContext(Dispatchers.IO) {
            feedbackRepository.createFeedback(
                title = param.title,
                content = param.content,
            )
        }
    }

    data class Param(
        val title: String,
        val content: String,
    )

    override fun handleException(exception: DataException) {
        super.handleException(exception)

        when(exception) {
            is CreateFeedbackException -> throw CreateFeedbackUseCaseException()

            else -> throw CreateFeedbackUseCaseException()
        }
    }
}