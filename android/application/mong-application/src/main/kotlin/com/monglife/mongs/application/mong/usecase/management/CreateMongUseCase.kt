package com.monglife.mongs.application.mong.usecase.management

import com.monglife.core.application.usecase.BaseParamUseCase
import com.monglife.mongs.application.mong.exception.InvalidCreateMongException
import com.monglife.mongs.application.mong.port.persistence.DevicePersistencePort
import com.monglife.mongs.application.mong.port.persistence.ManagementPersistencePort
import com.monglife.mongs.application.mong.port.web.ManagementWebPort
import com.monglife.mongs.application.mong.vo.MongVo
import com.monglife.mongs.domain.mong.model.MongOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalTime
import javax.inject.Inject

/**
 * 몽 생성 UseCase
 */
class CreateMongUseCase @Inject constructor(
    private val managementWebPort: ManagementWebPort,
    private val managementPersistencePort: ManagementPersistencePort,
    private val devicePersistencePort: DevicePersistencePort,
) : BaseParamUseCase<CreateMongUseCase.Command, MongVo>() {

    @Throws(InvalidCreateMongException::class)
    override suspend fun execute(command: Command): MongVo = withContext(Dispatchers.IO) {
        // 몽 생성 요청
        managementWebPort.createMong(
            name = command.name,
            sleepAt = command.sleepAt,
            wakeupAt = command.wakeupAt,
        ).let { response ->
            // 도메인 변환
            response.toDomain().let { mong ->
                // 몽 옵션 로컬 등록
                managementPersistencePort.saveMongOption(
                    mongOption = MongOption(
                        mongId = response.mongId,
                        graduateCheck = false,
                    )
                )
                // 몽 로컬 등록
                managementPersistencePort.saveMong(mong = mong)
                // 현재 몽으로 설정
                devicePersistencePort.setCurrentMongId(mongId = mong.mongId)

                val mongOption = managementPersistencePort.getMongOption(mongId = mong.mongId)
                    ?: managementPersistencePort.saveMongOption(
                        mongOption = MongOption(
                            mongId = mong.mongId,
                            graduateCheck = false,
                        )
                    )

                MongVo.of(mong = mong, mongOption = mongOption)
            }
        }
    }

    data class Command(
        val name: String,
        val sleepAt: LocalTime,
        val wakeupAt: LocalTime,
    )
}