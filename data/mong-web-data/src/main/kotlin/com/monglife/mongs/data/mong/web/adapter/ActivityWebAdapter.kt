package com.monglife.mongs.data.mong.web.adapter

import com.monglife.mongs.application.mong.port.web.ActivityWebPort
import com.monglife.mongs.application.mong.port.web.response.GetTrainingResponseVo
import com.monglife.mongs.application.mong.port.web.response.TrainingResponseVo
import javax.inject.Inject

class ActivityWebAdapter @Inject constructor(

) : ActivityWebPort {

    override suspend fun getTrainings(): List<GetTrainingResponseVo> {
        TODO("Not yet implemented")
    }

    override suspend fun getTraining(trainingCode: String): GetTrainingResponseVo {
        TODO("Not yet implemented")
    }

    override suspend fun training(
        trainingCode: String,
        mongId: Long,
        score: Int
    ): TrainingResponseVo {
        TODO("Not yet implemented")
    }
}