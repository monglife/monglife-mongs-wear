package com.monglife.mongs.application.mong.usecase.management

import com.monglife.mongs.application.mong.exception.InvalidEvolutionMongException
import com.monglife.mongs.application.mong.exception.NotFoundMongException
import com.monglife.mongs.application.mong.port.persistence.ManagementPersistencePort
import com.monglife.mongs.application.mong.port.web.ManagementWebPort
import com.monglife.mongs.application.mong.vo.MongVo
import com.monglife.core.application.usecase.BaseParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 몽 진화 UseCase
 */
class EvolutionMongUseCase @Inject constructor(
    private val managementWebPort: ManagementWebPort,
    private val managementPersistencePort: ManagementPersistencePort
) : BaseParamUseCase<EvolutionMongUseCase.Command, MongVo>() {

    @Throws(InvalidEvolutionMongException::class, NotFoundMongException::class)
    override suspend fun execute(command: Command): MongVo = withContext(Dispatchers.IO) {
        // 몽 진화 요청
        managementWebPort.evolutionMong(mongId = command.mongId).let { response ->
            managementPersistencePort.getMong(mongId = command.mongId).let { mong ->
                // 몽 진화
                mong.evolution(
                    mongCode = response.mongCode,
                    level = response.level,
                    expRatio = response.expRatio,
                    strengthRatio = response.strengthRatio,
                    healthyRatio = response.healthyRatio,
                    satietyRatio = response.satietyRatio,
                    fatigueRatio = response.fatigueRatio,
                    stateCode = response.stateCode,
                    statusCode = response.statusCode,
                    createdAt = response.createdAt,
                    updatedAt = response.updatedAt,
                )
                // 몽 로컬 등록
                managementPersistencePort.saveMong(mong = mong)
                // MongVo 반환
                managementPersistencePort.getMongOption(mongId = mong.mongId).let {
                    MongVo.of(mong = mong, mongOption = it)
                }
            }
        }
    }

    data class Command(
        val mongId: Long,
    )
}