package com.monglife.mongs.application.mong.port.web

import com.monglife.mongs.application.mong.exception.InvalidTrainingException
import com.monglife.mongs.application.mong.exception.NotFoundTrainingException
import com.monglife.mongs.application.mong.port.web.response.GetTrainingResponseVo
import com.monglife.mongs.application.mong.port.web.response.TrainingResponseVo

interface ActivityWebPort {

    /**
     * 훈련 목록 조회
     */
    suspend fun getTrainings(): List<GetTrainingResponseVo>

    /**
     * 훈련 조회
     */
    @Throws(NotFoundTrainingException::class)
    suspend fun getTraining(trainingCode: String): GetTrainingResponseVo

    /**
     * 훈련 완료
     */
    @Throws(InvalidTrainingException::class)
    suspend fun training(trainingCode: String, mongId: Long, score: Int): TrainingResponseVo
}