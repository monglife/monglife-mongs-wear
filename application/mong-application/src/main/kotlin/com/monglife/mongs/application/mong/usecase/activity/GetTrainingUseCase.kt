package com.monglife.mongs.application.mong.usecase.activity

import com.monglife.mongs.application.mong.exception.NotFoundTrainingException
import com.monglife.mongs.application.mong.port.web.ActivityWebPort
import com.monglife.mongs.application.mong.vo.TrainingTypeVo
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 훈련 조회 UseCase
 */
class GetTrainingUseCase @Inject constructor(
    private val activityWebPort: ActivityWebPort,
) : BaseParamUseCase<GetTrainingUseCase.Command, TrainingTypeVo>() {

    @Throws(NotFoundTrainingException::class)
    override suspend fun execute(command: Command): TrainingTypeVo {
        return withContext(Dispatchers.IO) {
            // 훈련 타입 조회 요청
            activityWebPort.getTraining(trainingCode = command.trainingCode).let { response ->
                // TrainingTypeVo 반환
                TrainingTypeVo.of(trainingType = response.toDomain())
            }
        }
    }

    data class Command(
        val trainingCode: String,
    )
}