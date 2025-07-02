package com.monglife.mongs.application.mong.usecase.management

import com.monglife.mongs.application.mong.exception.InvalidDeleteMongException
import com.monglife.mongs.application.mong.port.persistence.DevicePersistencePort
import com.monglife.mongs.application.mong.port.persistence.ManagementPersistencePort
import com.monglife.mongs.application.mong.port.web.ManagementWebPort
import com.monglife.core.application.usecase.BaseParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 몽 삭제 UseCase
 */
class DeleteMongUseCase @Inject constructor(
    private val managementWebPort: ManagementWebPort,
    private val managementPersistencePort: ManagementPersistencePort,
    private val devicePersistencePort: DevicePersistencePort,
) : BaseParamUseCase<DeleteMongUseCase.Command, Unit>() {

    @Throws(InvalidDeleteMongException::class)
    override suspend fun execute(command: Command) {
        withContext(Dispatchers.IO) {
            // 몽 삭제 요청
            managementWebPort.deleteMong(mongId = command.mongId).let { response ->
                // 몽 옵션 로컬 삭제
                managementPersistencePort.deleteMongOption(mongId = response.mongId)
                // 몽 로컬 삭제
                managementPersistencePort.deleteMong(mongId = response.mongId)
                // 몽 ID가 현재 몽 ID인 경우
                if (devicePersistencePort.getCurrentMongId() == response.mongId) {
                    // 현재 몽 ID 삭제
                    devicePersistencePort.deleteCurrentMongId()
                }
            }
        }
    }

    data class Command(
        val mongId: Long,
    )
}