package com.monglife.mongs.application.member.feedback.usecase

import com.monglife.mongs.application.member.feedback.exception.InvalidCreateFeedbackException
import com.monglife.mongs.application.member.feedback.port.persistence.DevicePersistencePort
import com.monglife.mongs.application.member.feedback.port.web.FeedbackWebPort
import com.monglife.core.application.usecase.BaseParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 오류 신고 등록 UseCase
 */
class CreateFeedbackUseCase @Inject constructor(
    private val devicePersistencePort: DevicePersistencePort,
    private val feedbackWebPort: FeedbackWebPort,
) : BaseParamUseCase<CreateFeedbackUseCase.Command, Unit>() {

    @Throws(InvalidCreateFeedbackException::class)
    override suspend fun execute(command: Command) {
        withContext(Dispatchers.IO) {
            val deviceName = devicePersistencePort.getDeviceName()

            // 오류 신고 등록 요청
            feedbackWebPort.createFeedback(
                deviceName = deviceName,
                title = command.title,
                content = command.content,
            )
        }
    }

    data class Command(
        val title: String,
        val content: String,
    )
}