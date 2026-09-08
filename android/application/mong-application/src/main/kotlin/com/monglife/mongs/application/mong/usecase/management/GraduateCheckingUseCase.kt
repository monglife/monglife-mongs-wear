package com.monglife.mongs.application.mong.usecase.management

import com.monglife.core.application.usecase.BaseParamUseCase
import com.monglife.mongs.application.mong.port.persistence.DevicePersistencePort
import com.monglife.mongs.application.mong.port.persistence.ManagementPersistencePort
import com.monglife.mongs.domain.mong.model.MongOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 몽 졸업 확인 상태로 변경 UseCase
 */
class GraduateCheckingUseCase @Inject constructor(
    private val managementPersistencePort: ManagementPersistencePort,
) : BaseParamUseCase<GraduateCheckingUseCase.Command, Unit>() {

    override suspend fun execute(command: Command) {
        withContext(Dispatchers.IO) {
            // 몽 옵션 로컬 조회
            val mongOption = managementPersistencePort.getMongOption(mongId = command.mongId)
                ?: managementPersistencePort.saveMongOption(
                    mongOption = MongOption(
                        mongId = command.mongId,
                        graduateCheck = false,
                    )
                )

            mongOption.let {
                // 졸업 상태 사용자 확인
                it.graduateCheck()
                // 몽 옵션 영속화
                managementPersistencePort.saveMongOption(mongOption = it)
            }
        }
    }

    data class Command(
        val mongId: Long,
    )
}