package com.mongs.wear.domain.training.repository

import com.mongs.wear.core.enums.TrainingCode
import com.mongs.wear.core.exception.data.TrainingBasketballException
import com.mongs.wear.domain.training.model.TrainingEndModel
import com.mongs.wear.domain.training.model.TrainingModel

interface TrainingRepository {

    /**
     * 훈련 달리기 정보 조회
     * @throws GetTrainingException
     */
    suspend fun getTraining(trainingCode: TrainingCode): TrainingModel

    /**
     * 훈련 달리기 완료
     * @throws TrainingRunnerException
     */
    suspend fun trainingRunner(mongId: Long, score: Int): TrainingEndModel

    /**
     * 훈련 농구 완료
     * @throws TrainingBasketballException
     */
    suspend fun trainingBasketball(mongId: Long, score: Int): TrainingEndModel
}