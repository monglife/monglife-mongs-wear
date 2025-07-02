package com.monglife.mongs.application.mong.usecase.activity

import com.monglife.mongs.application.mong.port.web.ActivityWebPort
import com.monglife.mongs.application.mong.vo.TrainingTypeVo
import com.monglife.core.application.usecase.BaseNoParamUseCase
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
            // 훈련 타입 목록 조회 요청
            activityWebPort.getTrainings().map { response ->
                // TrainingTypeVo 반환
                TrainingTypeVo.of(trainingType = response.toDomain())
            }
        }
    }
}