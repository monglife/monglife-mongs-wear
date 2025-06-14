package com.monglife.mongs.application.mong.usecase

import com.monglife.mongs.application.mong.exception.InvalidGraduateMongException
import com.monglife.mongs.application.mong.exception.NotFoundMongException
import com.monglife.mongs.application.mong.port.persistence.ManagementPersistencePort
import com.monglife.mongs.application.mong.port.web.ManagementWebPort
import com.monglife.mongs.application.mong.vo.MongVo
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 몽 졸업 UseCase
 */
class GraduateMongUseCase @Inject constructor(
    private val managementWebPort: ManagementWebPort,
    private val managementPersistencePort: ManagementPersistencePort,
) : BaseParamUseCase<GraduateMongUseCase.Command, Unit>() {

    @Throws(NotFoundMongException::class, InvalidGraduateMongException::class)
    override suspend fun execute(command: Command) {
        return withContext(Dispatchers.IO) {
            // 몽 조회 요청
            managementWebPort.getMong(mongId = command.mongId).let {
                val mong = it.toDomain()
                // 몽 졸업 요청
                managementWebPort.graduateMong(mongId = mong.mongId).let { response ->
                    // 몽 로컬 삭제
                    managementPersistencePort.deleteMong(mongId = response.mongId)
                }
            }
        }
    }

    data class Command(
        val mongId: Long,
    )
}