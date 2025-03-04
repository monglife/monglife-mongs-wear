package com.mongs.wear.domain.training.usecase

import com.mongs.wear.core.enums.TrainingCode
import com.mongs.wear.core.exception.data.TrainingBasketballException
import com.mongs.wear.core.exception.data.TrainingRunnerException
import com.mongs.wear.core.exception.global.DataException
import com.mongs.wear.core.exception.usecase.TrainingEndUseCaseException
import com.mongs.wear.core.usecase.BaseParamUseCase
import com.mongs.wear.domain.training.repository.TrainingRepository
import com.mongs.wear.domain.training.vo.TrainingEndVo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TrainingEndUseCase @Inject constructor(
    private val trainingRepository: TrainingRepository,
) : BaseParamUseCase<TrainingEndUseCase.Param, TrainingEndVo>() {

    /**
     * 훈련 완료 UseCase
     * @throws TrainingRunnerException
     */
    override suspend fun execute(param: Param): TrainingEndVo {
        return withContext(Dispatchers.IO) {
            val trainingEndModel = when(param.trainingCode) {
                // 훈련 달리기
                TrainingCode.RUNNER -> {
                    trainingRepository.trainingRunner(
                        mongId = param.mongId,
                        score = param.score,
                    )
                }

                // 훈련 농구
                TrainingCode.BASKETBALL -> {
                    trainingRepository.trainingBasketball(
                        mongId = param.mongId,
                        score = param.score,
                    )
                }
            }

            TrainingEndVo(
                isSuccess = trainingEndModel.isSuccess,
                rewardPayPoint = trainingEndModel.rewardPayPoint,
                score = trainingEndModel.score,
            )
        }
    }

    data class Param(

        val mongId: Long,

        val trainingCode: TrainingCode,

        val score: Int,
    )

    override fun handleException(exception: DataException) {
        super.handleException(exception)

        when(exception) {
            is TrainingRunnerException -> throw TrainingEndUseCaseException()

            is TrainingBasketballException -> throw TrainingEndUseCaseException()

            else -> throw TrainingEndUseCaseException()
        }
    }
}