package com.monglife.mongs.application.mong.usecase.management

import com.monglife.core.application.usecase.BaseNoParamUseCase
import com.monglife.mongs.application.mong.port.persistence.ManagementPersistencePort
import com.monglife.mongs.application.mong.port.web.ManagementWebPort
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 현재 몽 ID 옵저빙 UseCase
 */
@Singleton
class SyncRemoteMongsUseCase @Inject constructor(
    private val managementWebPort: ManagementWebPort,
    private val managementPersistencePort: ManagementPersistencePort,
) : BaseNoParamUseCase<Unit>() {

    override suspend fun execute() {
        // 몽 목록 조회 요청
        managementWebPort.getMongs().map { response ->
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
        }.also { mongs ->
            // 존재하지 않는 몽 로컬 삭제
            managementPersistencePort.deleteMongIfNotExistsMongIds(mongIds = mongs.map { it.mongId })
        }
    }
}