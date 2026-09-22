package com.monglife.mongs.application.mong.usecase.management

import com.monglife.core.application.usecase.BaseNoParamUseCase
import com.monglife.mongs.application.mong.exception.NotFoundMongException
import com.monglife.mongs.application.mong.port.persistence.DevicePersistencePort
import com.monglife.mongs.application.mong.port.persistence.ManagementPersistencePort
import com.monglife.mongs.application.mong.port.web.ManagementWebPort
import com.monglife.mongs.application.mong.vo.MongVo
import com.monglife.mongs.domain.mong.model.MongOption
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

    @Throws(NotFoundMongException::class)
    override suspend fun execute(): MongVo? = withContext(Dispatchers.IO) {
        devicePersistencePort.getCurrentMongId()?.let {
            managementWebPort.getMong(mongId = it).let { response ->
                // 몽 로컬 조회
                val mong = managementPersistencePort.getMong(mongId = response.mongId)
                    ?: managementPersistencePort.saveMong(mong = response.toDomain())

                mong.let {
                    // 몽 업데이트
                    it.update(
                        name = response.name,
                        mongCode = response.mongCode,
                        mongName = response.mongName,
                        stateCode = response.stateCode,
                        statusCode = response.statusCode,
                        level = response.level,
                        sleepAt = response.sleepAt,
                        wakeupAt = response.wakeupAt,
                        payPoint = response.payPoint,
                        isSleep = response.isSleep,
                        strengthRatio = response.strengthRatio,
                        healthyRatio = response.healthyRatio,
                        satietyRatio = response.satietyRatio,
                        fatigueRatio = response.fatigueRatio,
                        expRatio = response.expRatio,
                        weight = response.weight,
                        poopCount = response.poopCount,
                        randomDrawTicketCount = response.randomDrawTicketCount,
                        createdAt = response.createdAt,
                        updatedAt = response.updatedAt,
                    )
                    // 몽 로컬 등록
                    managementPersistencePort.saveMong(mong = it)
                }
                // MongVo 반환
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
}