package com.monglife.mongs.application.mong.usecase

import com.monglife.mongs.application.mong.exception.InvalidFeedSnackException
import com.monglife.mongs.application.mong.exception.NotFoundMongException
import com.monglife.mongs.application.mong.port.persistence.ManagementPersistencePort
import com.monglife.mongs.application.mong.port.web.InteractionWebPort
import com.monglife.mongs.application.mong.port.web.ManagementWebPort
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 몽 먹이 주기 UseCase
 */
class FeedSnackMongUseCase @Inject constructor(
    private val interactionWebPort: InteractionWebPort,
    private val managementWebPort: ManagementWebPort,
    private val managementPersistencePort: ManagementPersistencePort,
) : BaseParamUseCase<FeedSnackMongUseCase.Command, Unit>() {

    @Throws(NotFoundMongException::class, InvalidFeedSnackException::class)
    override suspend fun execute(command: Command) {
        withContext(Dispatchers.IO) {
            // 몽 조회 요청
            managementWebPort.getMong(mongId = command.mongId).let {
                val mong = it.toDomain()
                // 몽 간식 섭취 요청
                interactionWebPort.feedSnackMong(
                    mongId = mong.mongId,
                    snackCode = command.snackCode,
                ).let { response ->
                    // 몽 섭취
                    mong.feed(
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
                }
            }
        }
    }

    data class Command(
        val mongId: Long,
        val snackCode: String,
    )
}