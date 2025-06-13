package com.monglife.mongs.application.mong.usecase

import com.monglife.mongs.application.mong.exception.InvalidStrokeMongException
import com.monglife.mongs.application.mong.exception.NotFoundMongException
import com.monglife.mongs.application.mong.port.persistence.ManagementPersistencePort
import com.monglife.mongs.application.mong.port.web.ManagementWebPort
import com.monglife.mongs.application.mong.vo.MongVo
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 몽 쓰다 듬기 UseCase
 */
class StrokeMongUseCase @Inject constructor(
    private val managementWebPort: ManagementWebPort,
    private val managementPersistencePort: ManagementPersistencePort,
) : BaseParamUseCase<StrokeMongUseCase.Command, MongVo>() {

    @Throws(NotFoundMongException::class, InvalidStrokeMongException::class)
    override suspend fun execute(command: Command): MongVo {
        return withContext(Dispatchers.IO) {
            managementWebPort.getMong(mongId = command.mongId).let {
                // 몽 도메인 변환
                val mong = it.toDomain()
                // 몽 쓰다듬기 요청
                managementWebPort.strokeMong(mongId = mong.mongId).let { response ->
                    // 몽 쓰다듬기
                    mong.stroke(
                        mongId = response.mongId,
                        expRatio = response.expRatio,
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