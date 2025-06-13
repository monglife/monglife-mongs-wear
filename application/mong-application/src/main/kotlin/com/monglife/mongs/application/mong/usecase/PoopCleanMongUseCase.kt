package com.monglife.mongs.application.mong.usecase

import com.monglife.mongs.application.mong.exception.InvalidPoopCleanMongException
import com.monglife.mongs.application.mong.exception.NotFoundMongException
import com.monglife.mongs.application.mong.port.persistence.ManagementPersistencePort
import com.monglife.mongs.application.mong.port.web.ManagementWebPort
import com.monglife.mongs.application.mong.vo.MongVo
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
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
            managementWebPort.getMong(mongId = command.mongId).let {
                // 몽 도메인 변환
                val mong = it.toDomain()
                // 몽 배변 처리 요청
                managementWebPort.poopCleanMong(mongId = mong.mongId).let { response ->
                    // 몽 배변 처리
                    mong.poopClean(
                        mongId = response.mongId,
                        expRatio = response.expRatio,
                        poopCount = response.poopCount,
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