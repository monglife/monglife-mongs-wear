package com.monglife.mongs.application.mong.usecase

import com.monglife.mongs.application.mong.exception.InvalidEvolutionMongException
import com.monglife.mongs.application.mong.exception.NotFoundMongException
import com.monglife.mongs.application.mong.port.persistence.ManagementPersistencePort
import com.monglife.mongs.application.mong.port.web.ManagementWebPort
import com.monglife.mongs.application.mong.vo.MongVo
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
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

    @Throws(NotFoundMongException::class, InvalidEvolutionMongException::class)
    override suspend fun execute(command: Command): MongVo {
        return withContext(Dispatchers.IO) {
            // 몽 조회 요청
            managementWebPort.getMong(mongId = command.mongId).let {
                // 몽 도메인 변환
                val mong = it.toDomain()
                // 몽 진화 요청
                managementWebPort.evolutionMong(mongId = mong.mongId).let { response ->
                    // 몽 진화
                    mong.evolution(
                        mongCode = response.mongCode,
                        expRatio = response.expRatio,
                        strengthRatio = response.strengthRatio,
                        healthyRatio = response.healthyRatio,
                        satietyRatio = response.satietyRatio,
                        fatigueRatio = response.fatigueRatio,
                        createdAt = response.createdAt,
                        updatedAt = response.updatedAt,
                    )
                    // 몽 로컬 등록
                    managementPersistencePort.saveMong(mong = mong)
                    // MongVo 반환
                    MongVo.of(mong = mong)
                }
            }
        }
    }

    data class Command(
        val mongId: Long,
    )
}