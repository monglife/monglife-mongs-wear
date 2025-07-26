package com.monglife.mongs.application.mong.usecase.management

import com.monglife.core.application.usecase.BaseParamUseCase
import com.monglife.mongs.application.mong.exception.InvalidGraduateMongException
import com.monglife.mongs.application.mong.port.persistence.DevicePersistencePort
import com.monglife.mongs.application.mong.port.persistence.ManagementPersistencePort
import com.monglife.mongs.application.mong.port.web.ManagementWebPort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 몽 졸업 UseCase
 */
class GraduateMongUseCase @Inject constructor(
    private val managementWebPort: ManagementWebPort,
    private val managementPersistencePort: ManagementPersistencePort,
    private val devicePersistencePort: DevicePersistencePort,
) : BaseParamUseCase<GraduateMongUseCase.Command, Unit>() {

    @Throws(InvalidGraduateMongException::class)
    override suspend fun execute(command: Command) {
        return withContext(Dispatchers.IO) {
            // 몽 졸업 요청
            managementWebPort.graduateMong(mongId = command.mongId).let { response ->
                // 몽 옵션 로컬 삭제
                managementPersistencePort.deleteMongOption(mongId = response.mongId)
                // 몽 로컬 삭제
                managementPersistencePort.deleteMong(mongId = response.mongId)
                // 현재 몽 해제
                devicePersistencePort.deleteCurrentMongId()
            }
        }
    }

    data class Command(
        val mongId: Long,
    )
}