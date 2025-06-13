package com.monglife.mongs.application.mong.usecase

import com.monglife.mongs.application.mong.exception.InvalidCreateMongException
import com.monglife.mongs.application.mong.port.persistence.ManagementPersistencePort
import com.monglife.mongs.application.mong.port.web.ManagementWebPort
import com.monglife.mongs.application.mong.vo.MongVo
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalTime
import javax.inject.Inject

/**
 * 몽 생성 UseCase
 */
class CreateMongUseCase @Inject constructor(
    private val managementWebPort: ManagementWebPort,
    private val managementPersistencePort: ManagementPersistencePort
) : BaseParamUseCase<CreateMongUseCase.Command, MongVo>() {

    @Throws(InvalidCreateMongException::class)
    override suspend fun execute(command: Command): MongVo {
        return withContext(Dispatchers.IO) {
            // 몽 생성
            managementWebPort.createMong(
                name = command.name,
                sleepAt = command.sleepAt,
                wakeupAt = command.wakeupAt,
            ).let {
                // 도메인 변환
                val mong = it.toDomain()
                // 현재 몽으로 설정
                mong.selectAsActive()
                // 몽 저장
                managementPersistencePort.saveMong(mong = mong)
                // MongVo 반환
                MongVo.of(mong = mong)
            }
        }
    }

    data class Command(
        val name: String,
        val sleepAt: LocalTime,
        val wakeupAt: LocalTime,
    )
}