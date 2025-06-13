package com.monglife.mongs.application.mong.usecase

import com.monglife.mongs.application.mong.exception.NotFoundMongException
import com.monglife.mongs.application.mong.port.persistence.ManagementPersistencePort
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
import com.monglife.mongs.domain.mong.model.Mong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 몽 졸업 확인 상태로 변경 UseCase
 */
class GraduateCheckingUseCase @Inject constructor(
    private val managementPersistencePort: ManagementPersistencePort,
) : BaseParamUseCase<GraduateCheckingUseCase.Command, Unit>() {

    @Throws(NotFoundMongException::class)
    override suspend fun execute(command: Command) {
        withContext(Dispatchers.IO) {
            managementPersistencePort.getMong(mongId = command.mongId).let { mong: Mong ->
                // 졸업 상태 사용자 확인
                mong.graduateChecking()
                // 몽 영속화
                managementPersistencePort.saveMong(mong = mong)
            }
        }
    }

    data class Command(
        val mongId: Long,
    )
}