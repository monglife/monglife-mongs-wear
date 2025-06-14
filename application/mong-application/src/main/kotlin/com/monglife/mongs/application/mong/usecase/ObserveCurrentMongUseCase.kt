package com.monglife.mongs.application.mong.usecase

import com.monglife.mongs.application.mong.exception.NotFoundMongException
import com.monglife.mongs.application.mong.port.persistence.ManagementPersistencePort
import com.monglife.mongs.application.mong.vo.MongVo
import com.monglife.mongs.core.domain.usecase.BaseNoParamUseCase
import com.monglife.mongs.domain.mong.model.Mong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 현재 슬롯 조회 UseCase
 */
class ObserveCurrentMongUseCase @Inject constructor(
    private val managementPersistencePort: ManagementPersistencePort,
) : BaseNoParamUseCase<Flow<MongVo?>>() {

    @Throws(NotFoundMongException::class)
    override suspend fun execute(): Flow<MongVo?> {
        return withContext(Dispatchers.IO) {
            // 현재 몽 ID 조회
            managementPersistencePort.getCurrentMongId()?.let { mongId: Long ->
                // 몽 Flow 로컬 조회
                managementPersistencePort.getMongFlow(mongId = mongId).map { mong: Mong ->
                    // MongVo 반환
                    MongVo.of(mong = mong)
                }
            } ?: flowOf(null)
        }
    }
}