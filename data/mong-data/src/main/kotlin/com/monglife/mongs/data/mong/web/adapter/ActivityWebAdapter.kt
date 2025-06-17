package com.monglife.mongs.data.mong.web.adapter

import com.monglife.mongs.application.mong.exception.InvalidTrainingException
import com.monglife.mongs.application.mong.exception.NotFoundTrainingException
import com.monglife.mongs.application.mong.port.web.ActivityWebPort
import com.monglife.mongs.application.mong.port.web.response.GetTrainingResponse
import com.monglife.mongs.application.mong.port.web.response.TrainingResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityWebAdapter @Inject constructor(

) : ActivityWebPort {

    /**
     * 훈련 목록 조회
     */
    override suspend fun getTrainings(): List<GetTrainingResponse> {
        TODO("Not yet implemented")
    }

    /**
     * 훈련 조회
     */
    @Throws(NotFoundTrainingException::class)
    override suspend fun getTraining(trainingCode: String): GetTrainingResponse {
        TODO("Not yet implemented")
    }

    /**
     * 훈련 완료
     */
    @Throws(InvalidTrainingException::class)
    override suspend fun training(
        trainingCode: String,
        mongId: Long,
        score: Int
    ): TrainingResponse {
        TODO("Not yet implemented")
    }
}