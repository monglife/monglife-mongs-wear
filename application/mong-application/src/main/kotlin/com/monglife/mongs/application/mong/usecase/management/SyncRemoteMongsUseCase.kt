package com.monglife.mongs.application.mong.usecase.management

import com.monglife.mongs.application.mong.exception.NotFoundMongException
import com.monglife.mongs.application.mong.port.persistence.ManagementPersistencePort
import com.monglife.mongs.application.mong.port.web.ManagementWebPort
import com.monglife.mongs.core.domain.usecase.BaseNoParamUseCase
import com.monglife.mongs.domain.mong.model.MongOption
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
            try {
                managementPersistencePort.getMong(mongId = response.mongId).let { mong ->
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
                }
            } catch (e: NotFoundMongException) {
                // 몽 옵션 로컬 등록
                managementPersistencePort.saveMongOption(
                    mongOption = MongOption(
                        mongId = response.mongId,
                        graduateCheck = false,
                    )
                )
                // 몽 로컬 등록
                managementPersistencePort.saveMong(mong = response.toDomain())
            }
        }.also { mongs ->
            // 존재하지 않는 몽 로컬 삭제
            managementPersistencePort.deleteMongIfNotExistsMongIds(mongIds = mongs.map { it.mongId })
        }
    }
}