package com.monglife.mongs.application.mong.usecase.management

import com.monglife.mongs.application.mong.port.persistence.ManagementPersistencePort
import com.monglife.mongs.application.mong.vo.MongVo
import com.monglife.mongs.core.domain.usecase.BaseNoParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 현재 몽 ID 옵저빙 UseCase
 */
class ObserveCurrentMongUseCase @Inject constructor(
    private val managementPersistencePort: ManagementPersistencePort,
    ) : BaseNoParamUseCase<Flow<MongVo?>>() {

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun execute(): Flow<MongVo?> =

        managementPersistencePort.getCurrentMongIdFlow()
            .flatMapLatest { mongId ->
                mongId?.let {
                    managementPersistencePort.getMongFlow(mongId)
                        .map { mong ->
                            mong?.let {
                                managementPersistencePort.getMongOption(mongId = mong.mongId).let {
                                    MongVo.of(mong = mong, mongOption = it)
                                }
                            }
                        }
                } ?: flowOf(null)
            }
            .flowOn(Dispatchers.IO)
}