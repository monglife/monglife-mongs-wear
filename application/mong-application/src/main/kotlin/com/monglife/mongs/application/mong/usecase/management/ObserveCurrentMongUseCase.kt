package com.monglife.mongs.application.mong.usecase.management

import com.monglife.core.application.usecase.BaseNoParamUseCase
import com.monglife.mongs.application.mong.port.persistence.DevicePersistencePort
import com.monglife.mongs.application.mong.port.persistence.ManagementPersistencePort
import com.monglife.mongs.application.mong.vo.MongVo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 현재 몽 ID 옵저빙 UseCase
 */
@Singleton
class ObserveCurrentMongUseCase @Inject constructor(
    private val managementPersistencePort: ManagementPersistencePort,
    private val devicePersistencePort: DevicePersistencePort,
) : BaseNoParamUseCase<Flow<MongVo?>>() {

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun execute(): Flow<MongVo?> {
        return devicePersistencePort.getCurrentMongIdFlow()
            .flatMapLatest { mongId ->
                mongId?.let {
                    managementPersistencePort.getMongFlow(mongId = mongId)
                        .map { mong ->
                            mong?.let {
                                managementPersistencePort.getMongOption(mongId = mong.mongId)
                                    .let { mongOption ->
                                        MongVo.of(mong = mong, mongOption = mongOption)
                                    }
                            }
                        }
                } ?: flowOf(null)
            }.flowOn(Dispatchers.IO)
    }
}