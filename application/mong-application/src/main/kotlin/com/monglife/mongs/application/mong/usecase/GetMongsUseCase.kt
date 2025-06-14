package com.monglife.mongs.application.mong.usecase

import com.monglife.mongs.application.mong.exception.NotFoundMongException
import com.monglife.mongs.application.mong.port.persistence.ManagementPersistencePort
import com.monglife.mongs.application.mong.port.web.ManagementWebPort
import com.monglife.mongs.application.mong.vo.MongVo
import com.monglife.mongs.core.domain.usecase.BaseNoParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 몽 목록 조회 UseCase
 */
class GetMongsUseCase @Inject constructor(
    private val managementWebPort: ManagementWebPort,
    private val managementPersistencePort: ManagementPersistencePort,
) : BaseNoParamUseCase<List<MongVo>>() {

    @Throws(NotFoundMongException::class)
    override suspend fun execute(): List<MongVo> {
        return withContext(Dispatchers.IO) {
            // 몽 목록 조회 요청
            managementWebPort.getMongs().map { response ->
                // 몽 로컬 조회
                val mong = managementPersistencePort.getMong(mongId = response.mongId)
                // 몽 업데이트
                mong.update(
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
                managementPersistencePort.saveMong(mong = mong)
                // MongVo 반환
                MongVo.of(mong = mong)
            }
        }
    }
}