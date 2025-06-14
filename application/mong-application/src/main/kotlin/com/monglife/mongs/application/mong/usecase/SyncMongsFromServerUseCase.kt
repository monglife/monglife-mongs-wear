package com.monglife.mongs.application.mong.usecase

import com.monglife.mongs.application.mong.port.persistence.AuthPersistencePort
import com.monglife.mongs.application.mong.port.persistence.ManagementPersistencePort
import com.monglife.mongs.application.mong.port.web.ManagementWebPort
import com.monglife.mongs.core.domain.usecase.BaseNoParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 몽 동기화 UseCase
 */
class SyncMongsFromServerUseCase @Inject constructor(
    private val managementWebPort: ManagementWebPort,
    private val managementPersistencePort: ManagementPersistencePort,
    private val authPersistencePort: AuthPersistencePort,
) : BaseNoParamUseCase<Unit>() {

    override suspend fun execute() {
        withContext(Dispatchers.IO) {
            // 로그인 여부 확인
            if (authPersistencePort.isExistsSession()) {
                // 몽 목록 조회 요청
                managementWebPort.getMongs().forEach {
                    // 몽 로컬 등록
                    managementPersistencePort.saveMong(mong = it.toDomain())
                }
            }
        }
    }
}