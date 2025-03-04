package com.mongs.wear.domain.training.usecase

import com.mongs.wear.core.enums.TrainingCode
import com.mongs.wear.core.exception.data.GetTrainingException
import com.mongs.wear.core.exception.global.DataException
import com.mongs.wear.core.usecase.BaseParamUseCase
import com.mongs.wear.core.exception.usecase.GetTrainingUseCaseException
import com.mongs.wear.domain.training.repository.TrainingRepository
import com.mongs.wear.domain.training.vo.TrainingVo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetTrainingUseCase @Inject constructor(
    private val trainingRepository: TrainingRepository,
) : BaseParamUseCase<GetTrainingUseCase.Param, TrainingVo>() {

    /**
     * 훈련 보상 정보 조회 UseCase
     * @throws GetTrainingException
     */
    override suspend fun execute(param: Param): TrainingVo {
        return withContext(Dispatchers.IO) {

            val trainingCode = param.trainingCode

            val trainingModel = trainingRepository.getTraining(trainingCode = trainingCode)

            TrainingVo(
                rewardPayPoint = trainingModel.rewardPayPoint,
                score = trainingModel.score,
                timeout = trainingModel.timeout,
            )
        }
    }

    data class Param(

        val trainingCode: TrainingCode,
    )

    override fun handleException(exception: DataException) {
        super.handleException(exception)

        when(exception) {
            is GetTrainingException -> throw GetTrainingUseCaseException()

            else -> throw GetTrainingUseCaseException()
        }
    }
}