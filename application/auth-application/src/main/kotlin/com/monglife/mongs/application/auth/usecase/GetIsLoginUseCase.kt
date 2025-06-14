package com.monglife.mongs.application.auth.usecase

import com.monglife.mongs.application.auth.port.persistence.AuthPersistencePort
import com.monglife.mongs.core.domain.usecase.BaseNoParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 로그인 여부 라이브 객체 조회 UseCase
 */
class GetIsLoginUseCase @Inject constructor(
    private val authPersistencePort: AuthPersistencePort,
) : BaseNoParamUseCase<Flow<Boolean>>() {

    override suspend fun execute(): Flow<Boolean> {
        return withContext(Dispatchers.IO) {
            // session 존재 여부 로컬 조회
            authPersistencePort.isExistsSessionFlow()
        }
    }
}