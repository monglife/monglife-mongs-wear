package com.monglife.mongs.application.mong.usecase.activity

import com.monglife.core.application.usecase.BaseNoParamUseCase
import com.monglife.mongs.application.mong.port.web.ActivityWebPort
import com.monglife.mongs.application.mong.vo.TrainingTypeVo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 훈련 목록 조회 UseCase
 */
class GetTrainingsUseCase @Inject constructor(
    private val activityWebPort: ActivityWebPort,
) : BaseNoParamUseCase<List<TrainingTypeVo>>() {

    override suspend fun execute(): List<TrainingTypeVo> {
        return withContext(Dispatchers.IO) {
            activityWebPort.getTrainings().map {
                TrainingTypeVo.of(trainingType = it.toDomain())
            }
        }
    }
}