package com.monglife.mongs.application.mong.usecase.management

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
            // 몽 수면, 기상 요청
            managementWebPort.sleepingMong(mongId = command.mongId).let { response ->
                managementPersistencePort.getMong(mongId = command.mongId).let { mong ->
                    // 몽 수면, 기상
                    mong.sleeping(
                        mongId = response.mongId,
                        isSleep = response.isSleep,
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
    }

    data class Command(
        val mongId: Long,
    )
}