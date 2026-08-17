package com.monglife.mongs.application.mong.usecase.management

import com.monglife.core.application.usecase.BaseNoParamUseCase
import com.monglife.mongs.application.mong.port.persistence.DevicePersistencePort
import com.monglife.mongs.application.mong.port.persistence.ManagementPersistencePort
import com.monglife.mongs.application.mong.vo.MongVo
import com.monglife.mongs.domain.mong.model.MongOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
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
                    /**
                     * 몽 옵션을 Flow 로 결합한다.
                     * 이전에는 map 안에서 emission 마다 getMongOption 을 호출해
                     * MQTT 푸시 1건마다 DB 왕복이 한 번씩 더 발생했다.
                     */
                    ensureMongOption(mongId = mongId)

                    combine(
                        managementPersistencePort.getMongFlow(mongId = mongId),
                        managementPersistencePort.getMongOptionFlow(mongId = mongId),
                    ) { mong, mongOption ->
                        if (mong == null || mongOption == null) null
                        else MongVo.of(mong = mong, mongOption = mongOption)
                    }
                } ?: flowOf(null)
            }
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)
    }

    /**
     * 몽 옵션이 없으면 기본값으로 만들어 둔다.
     */
    private suspend fun ensureMongOption(mongId: Long) {
        if (managementPersistencePort.getMongOption(mongId = mongId) == null) {
            managementPersistencePort.saveMongOption(
                mongOption = MongOption(
                    mongId = mongId,
                    graduateCheck = false,
                )
            )
        }
    }
}