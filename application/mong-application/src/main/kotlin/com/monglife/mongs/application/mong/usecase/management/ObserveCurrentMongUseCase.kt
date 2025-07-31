package com.monglife.mongs.application.mong.usecase.management

import com.monglife.core.application.usecase.BaseNoParamUseCase
import com.monglife.mongs.application.mong.port.persistence.DevicePersistencePort
import com.monglife.mongs.application.mong.port.persistence.ManagementPersistencePort
import com.monglife.mongs.application.mong.vo.MongVo
import com.monglife.mongs.domain.mong.model.MongOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
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
                } ?: flowOf(null)
            }.flowOn(Dispatchers.IO)
    }
}