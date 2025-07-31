package com.monglife.mongs.application.mong.usecase.management

import com.monglife.core.application.usecase.BaseParamUseCase
import com.monglife.mongs.application.mong.exception.InvalidSleepingMongException
import com.monglife.mongs.application.mong.exception.NotFoundMongException
import com.monglife.mongs.application.mong.port.persistence.ManagementPersistencePort
import com.monglife.mongs.application.mong.port.web.ManagementWebPort
import com.monglife.mongs.application.mong.vo.MongVo
import com.monglife.mongs.domain.mong.model.MongOption
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
                val mong = managementPersistencePort.getMong(mongId = command.mongId)
                    ?: managementWebPort.getMong(mongId = command.mongId).let {
                        managementPersistencePort.saveMong(mong = it.toDomain())
                    }

                mong.let {
                    // 몽 수면, 기상
                    it.sleeping(
                        mongId = response.mongId,
                        isSleep = response.isSleep,
                        createdAt = response.createdAt,
                        updatedAt = response.updatedAt,
                    )
                    // 몽 로컬 등록
                    managementPersistencePort.saveMong(mong = it)
                    // MongVo 반환
                    val mongOption = managementPersistencePort.getMongOption(mongId = it.mongId)
                        ?: managementPersistencePort.saveMongOption(
                            mongOption = MongOption(
                                mongId = it.mongId,
                                graduateCheck = false,
                            )
                        )

                    MongVo.of(mong = it, mongOption = mongOption)
                }
            }
        }
    }

    data class Command(
        val mongId: Long,
    )
}