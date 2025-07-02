package com.monglife.mongs.application.mong.usecase.activity

import com.monglife.mongs.application.mong.exception.InvalidTrainingException
import com.monglife.mongs.application.mong.exception.NotFoundMongException
import com.monglife.mongs.application.mong.port.persistence.ManagementPersistencePort
import com.monglife.mongs.application.mong.port.web.ActivityWebPort
import com.monglife.mongs.application.mong.vo.TrainingEndVo
import com.monglife.core.application.usecase.BaseParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 훈련 완료 UseCase
 */
class TrainingEndUseCase @Inject constructor(
    private val activityWebPort: ActivityWebPort,
    private val managementPersistencePort: ManagementPersistencePort,
) : BaseParamUseCase<TrainingEndUseCase.Command, TrainingEndVo>() {

    @Throws(NotFoundMongException::class, InvalidTrainingException::class)
    override suspend fun execute(command: Command): TrainingEndVo {
        return withContext(Dispatchers.IO) {
            // 몽 훈련 요청
            activityWebPort.training(
                mongId = command.mongId,
                trainingCode = command.trainingCode,
                score = command.score,
            ).let { response ->
                managementPersistencePort.getMong(mongId = command.mongId).let { mong ->
                    // 몽 훈련
                    mong.training(
                        mongId = response.mongId,
                        payPoint = response.payPoint,
                        expRatio = response.expRatio,
                        strengthRatio = response.strengthRatio,
                        healthyRatio = response.healthyRatio,
                        satietyRatio = response.satietyRatio,
                        fatigueRatio = response.fatigueRatio,
                        weight = response.weight,
                        stateCode = response.stateCode,
                        statusCode = response.statusCode,
                    )
                    // 몽 로컬 등록
                    managementPersistencePort.saveMong(mong = mong)
                    // TrainingEndVo 반환
                    TrainingEndVo(
                        isSuccess = response.isSuccess,
                        rewardPayPoint = response.rewardPayPoint,
                        score = response.score,
                    )
                }
            }
        }
    }

    data class Command(
        val mongId: Long,
        val trainingCode: String,
        val score: Int,
    )
}