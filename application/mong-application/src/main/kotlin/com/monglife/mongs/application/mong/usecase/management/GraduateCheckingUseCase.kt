package com.monglife.mongs.application.mong.usecase.management

import com.monglife.mongs.application.mong.exception.NotFoundMongOptionException
import com.monglife.mongs.application.mong.port.persistence.ManagementPersistencePort
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 몽 졸업 확인 상태로 변경 UseCase
 */
class GraduateCheckingUseCase @Inject constructor(
    private val managementPersistencePort: ManagementPersistencePort,
) : BaseParamUseCase<GraduateCheckingUseCase.Command, Unit>() {

    @Throws(NotFoundMongOptionException::class)
    override suspend fun execute(command: Command) {
        withContext(Dispatchers.IO) {
            managementPersistencePort.getCurrentMongId()?.let { currentMongId ->
                if (currentMongId == command.mongId) {
                    // 현재 몽 ID 삭제
                    managementPersistencePort.deleteCurrentMongId()
                }
            }

            // 몽 옵션 로컬 조회
            managementPersistencePort.getMongOption(mongId = command.mongId).let { mongOption ->
                // 졸업 상태 사용자 확인
                mongOption.graduateCheck()
                // 몽 옵션 영속화
                managementPersistencePort.saveMongOption(mongOption = mongOption)
            }
        }
    }

    data class Command(
        val mongId: Long,
    )
}