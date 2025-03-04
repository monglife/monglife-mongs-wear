package com.mongs.wear.data.activity.repository

import com.mongs.wear.core.enums.TrainingCode
import com.mongs.wear.core.exception.data.GetTrainingException
import com.mongs.wear.core.exception.data.TrainingBasketballException
import com.mongs.wear.core.exception.data.TrainingRunnerException
import com.mongs.wear.data.activity.api.TrainingApi
import com.mongs.wear.data.activity.dto.request.TrainingBasketballRequestDto
import com.mongs.wear.data.activity.dto.request.TrainingRunnerRequestDto
import com.mongs.wear.data.global.utils.HttpUtil
import com.mongs.wear.domain.training.model.TrainingEndModel
import com.mongs.wear.domain.training.model.TrainingModel
import com.mongs.wear.domain.training.repository.TrainingRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrainingRepositoryImpl @Inject constructor(
    private val trainingApi: TrainingApi,
) : TrainingRepository {

    /**
     * 훈련 보상 정보 조회
     * @throws GetTrainingException
     */
    override suspend fun getTraining(trainingCode: TrainingCode) : TrainingModel {

        val response = trainingApi.getTrainingRunner(trainingCode = trainingCode)

        if (response.isSuccessful) {
            response.body()?.let { body ->
                return TrainingModel(
                    rewardPayPoint = body.result.rewardPayPoint,
                    score = body.result.score,
                    timeout = body.result.timeout,
                )
            }
        }

        throw GetTrainingException(result = HttpUtil.getErrorResult(response.errorBody()))
    }

    /**
     * 훈련 달리기 완료
     * @throws TrainingRunnerException
     */
    override suspend fun trainingRunner(mongId: Long, score: Int): TrainingEndModel {

        val response = trainingApi.trainingRunner(
            trainingRunnerRequestDto = TrainingRunnerRequestDto(
                mongId = mongId,
                score = score,
            )
        )

        if (response.isSuccessful) {
            response.body()?.let { body ->
                return TrainingEndModel(
                    isSuccess = body.result.isSuccess,
                    rewardPayPoint = body.result.rewardPayPoint,
                    score = body.result.score,
                )
            }
        }

        throw TrainingRunnerException(result = HttpUtil.getErrorResult(response.errorBody()))
    }

    /**
     * 훈련 농구 완료
     * @throws TrainingBasketballException
     */
    override suspend fun trainingBasketball(mongId: Long, score: Int): TrainingEndModel {

        val response = trainingApi.trainingBasketball(
            trainingBasketballRequestDto = TrainingBasketballRequestDto(
                mongId = mongId,
                score = score,
            )
        )

        if (response.isSuccessful) {
            response.body()?.let { body ->
                return TrainingEndModel(
                    isSuccess = body.result.isSuccess,
                    rewardPayPoint = body.result.rewardPayPoint,
                    score = body.result.score,
                )
            }
        }

        throw TrainingBasketballException(result = HttpUtil.getErrorResult(response.errorBody()))
    }
}