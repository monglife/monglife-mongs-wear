package com.monglife.mongs.application.mong.usecase

import com.monglife.mongs.application.mong.exception.InvalidSleepingMongException
import com.monglife.mongs.application.mong.exception.NotFoundMongException
import com.monglife.mongs.application.mong.port.persistence.ManagementPersistencePort
import com.monglife.mongs.application.mong.port.web.ManagementWebPort
import com.monglife.mongs.application.mong.vo.MongVo
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 몽 수면/기상 UseCase
 */
class SleepingMongUseCase @Inject constructor(
    private val managementWebPort: ManagementWebPort,
    private val managementPersistencePort: ManagementPersistencePort,
) : BaseParamUseCase<SleepingMongUseCase.Command, MongVo>() {

    @Throws(NotFoundMongException::class, InvalidSleepingMongException::class)
    override suspend fun execute(command: Command): MongVo {
        return withContext(Dispatchers.IO) {
            managementWebPort.getMong(mongId = command.mongId).let {
                // 몽 도메인 변환
                val mong = it.toDomain()
                // 몽 수면, 기상 요청
                managementWebPort.sleepingMong(mongId = mong.mongId).let { response ->
                    // 몽 수면, 기상
                    mong.sleeping(
                        mongId = response.mongId,
                        isSleep = response.isSleep,
                        createdAt = response.createdAt,
                        updatedAt = response.updatedAt,
                    )
                    // 몽 영속화
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