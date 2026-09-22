package com.monglife.mongs.data.mong.web.adapter

import com.monglife.mongs.application.mong.exception.InvalidTrainingException
import com.monglife.mongs.application.mong.exception.NotFoundTrainingException
import com.monglife.mongs.application.mong.port.web.ActivityWebPort
import com.monglife.mongs.application.mong.port.web.response.GetTrainingResponse
import com.monglife.mongs.application.mong.port.web.response.TrainingResponse
import com.monglife.mongs.data.mong.web.client.ActivityWebClient
import com.monglife.mongs.data.mong.web.client.request.TrainingEndRequestDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityWebAdapter @Inject constructor(
    private val activityWebClient: ActivityWebClient,
) : ActivityWebPort {

    /**
     * 훈련 목록 조회
     */
    override suspend fun getTrainings(): List<GetTrainingResponse> =
        activityWebClient.getTrainingTypes().let { response ->

            val body = response.takeIf { it.isSuccessful }?.body()

            body?.result?.map {
                GetTrainingResponse(
                    trainingTypeId = it.trainingTypeId,
                    trainingCode = it.trainingCode,
                    trainingName = it.trainingName,
                    payPoint = it.payPoint,
                    score = it.score,
                    timeout = it.timeout,
                    exp = it.exp,
                    weight = it.weight,
                    strength = it.strength,
                    satiety = it.satiety,
                    fatigue = it.fatigue,
                )
            }
        } ?: emptyList()

    /**
     * 훈련 조회
     */
    @Throws(NotFoundTrainingException::class)
    override suspend fun getTraining(trainingCode: String): GetTrainingResponse =
        activityWebClient.getTrainingType(trainingCode = trainingCode).let { response ->

            val body =
                response.takeIf { it.isSuccessful }?.body() ?: throw NotFoundTrainingException()

            GetTrainingResponse(
                trainingTypeId = body.result.trainingTypeId,
                trainingCode = body.result.trainingCode,
                trainingName = body.result.trainingName,
                payPoint = body.result.payPoint,
                score = body.result.score,
                timeout = body.result.timeout,
                exp = body.result.exp,
                weight = body.result.weight,
                strength = body.result.strength,
                satiety = body.result.satiety,
                fatigue = body.result.fatigue,
            )
        }

    /**
     * 훈련 완료
     */
    @Throws(InvalidTrainingException::class)
    override suspend fun training(
        trainingCode: String,
        mongId: Long,
        score: Int
    ): TrainingResponse = activityWebClient.trainingEnd(
        trainingEndRequestDto = TrainingEndRequestDto(
            trainingCode = trainingCode,
            mongId = mongId,
            score = score,
        )
    ).let { response ->

        val body = response.takeIf { it.isSuccessful }?.body() ?: throw InvalidTrainingException()

        TrainingResponse(
            isSuccess = body.result.isSuccess,
            rewardPayPoint = body.result.rewardPayPoint,
            score = body.result.score,
            mongId = body.result.mongId,
            payPoint = body.result.payPoint,
            expRatio = body.result.expRatio,
            strengthRatio = body.result.strengthRatio,
            healthyRatio = body.result.healthyRatio,
            satietyRatio = body.result.satietyRatio,
            fatigueRatio = body.result.fatigueRatio,
            weight = body.result.weight,
            stateCode = body.result.stateCode,
            statusCode = body.result.statusCode,
        )
    }
}