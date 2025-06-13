package com.monglife.mongs.application.mong.usecase

import com.monglife.mongs.application.mong.port.web.ActivityWebPort
import com.monglife.mongs.application.mong.vo.TrainingTypeVo
import com.monglife.mongs.core.domain.usecase.BaseNoParamUseCase
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
            activityWebPort.getTrainings().map { response ->
                TrainingTypeVo.of(trainingType = response.toDomain())
            }
        }
    }
}