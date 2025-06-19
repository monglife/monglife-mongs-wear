package com.monglife.mongs.application.mong.usecase

import com.monglife.mongs.application.mong.port.persistence.ManagementPersistencePort
import com.monglife.mongs.application.mong.vo.MongVo
import com.monglife.mongs.core.domain.usecase.BaseNoParamUseCase
import com.monglife.mongs.domain.mong.model.Mong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 현재 슬롯 조회 UseCase
 */
class ObserveMongsUseCase @Inject constructor(
    private val managementPersistencePort: ManagementPersistencePort,
) : BaseNoParamUseCase<Flow<List<MongVo>>>() {

    override suspend fun execute(): Flow<List<MongVo>> {
        return withContext(Dispatchers.IO) {
            // 몽 Flow 목록 로컬 조회
            managementPersistencePort.getMongsFlow().map {
                it.map { mong: Mong ->
                    // MongVo 반환
                    MongVo.of(mong = mong)
                }
            }
        }
    }
}