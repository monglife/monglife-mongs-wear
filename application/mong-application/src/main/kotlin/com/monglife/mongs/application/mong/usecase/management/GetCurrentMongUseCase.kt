package com.monglife.mongs.application.mong.usecase.management

import com.monglife.mongs.application.mong.port.persistence.DevicePersistencePort
import com.monglife.mongs.application.mong.port.persistence.ManagementPersistencePort
import com.monglife.mongs.application.mong.port.web.ManagementWebPort
import com.monglife.mongs.application.mong.vo.MongVo
import com.monglife.mongs.core.domain.usecase.BaseNoParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 현재 몽 조회 UseCase
 */
class GetCurrentMongUseCase @Inject constructor(
    private val managementWebPort: ManagementWebPort,
    private val managementPersistencePort: ManagementPersistencePort,
    private val devicePersistencePort: DevicePersistencePort,
) : BaseNoParamUseCase<MongVo?>() {

    override suspend fun execute(): MongVo? = withContext(Dispatchers.IO) {
        devicePersistencePort.getCurrentMongId()?.let {
            managementWebPort.getMong(mongId = it).toDomain().let { mong ->
                managementPersistencePort.getMongOption(mongId = mong.mongId).let { mongOption ->
                    MongVo.of(mong = mong, mongOption = mongOption)
                }
            }
        }
    }
}