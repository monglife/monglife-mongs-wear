package com.monglife.mongs.application.mong.usecase.management

import com.monglife.mongs.application.mong.exception.InvalidPoopCleanMongException
import com.monglife.mongs.application.mong.exception.NotFoundMongException
import com.monglife.mongs.application.mong.port.persistence.ManagementPersistencePort
import com.monglife.mongs.application.mong.port.web.ManagementWebPort
import com.monglife.mongs.application.mong.vo.MongVo
import com.monglife.core.application.usecase.BaseParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 몽 배변 처리 UseCase
 */
class PoopCleanMongUseCase @Inject constructor(
    private val managementWebPort: ManagementWebPort,
    private val managementPersistencePort: ManagementPersistencePort,
) : BaseParamUseCase<PoopCleanMongUseCase.Command, MongVo>() {

    @Throws(NotFoundMongException::class, InvalidPoopCleanMongException::class)
    override suspend fun execute(command: Command): MongVo {
        return withContext(Dispatchers.IO) {
            // 몽 배변 처리 요청
            managementWebPort.poopCleanMong(mongId = command.mongId).let { response ->
                managementPersistencePort.getMong(mongId = command.mongId).let { mong ->
                    // 몽 배변 처리
                    mong.poopClean(
                        mongId = response.mongId,
                        expRatio = response.expRatio,
                        poopCount = response.poopCount,
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